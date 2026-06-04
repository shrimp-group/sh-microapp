package com.wkclz.micro.fileos.service.impl;

import com.aliyun.oss.ClientException;
import com.aliyun.oss.HttpMethod;
import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.OSSException;
import com.aliyun.oss.common.auth.CredentialsProviderFactory;
import com.aliyun.oss.common.auth.DefaultCredentialProvider;
import com.aliyun.oss.model.AbortMultipartUploadRequest;
import com.aliyun.oss.model.CannedAccessControlList;
import com.aliyun.oss.model.CompleteMultipartUploadRequest;
import com.aliyun.oss.model.DeleteObjectsRequest;
import com.aliyun.oss.model.DeleteObjectsResult;
import com.aliyun.oss.model.GeneratePresignedUrlRequest;
import com.aliyun.oss.model.GetObjectRequest;
import com.aliyun.oss.model.InitiateMultipartUploadRequest;
import com.aliyun.oss.model.InitiateMultipartUploadResult;
import com.aliyun.oss.model.ObjectMetadata;
import com.aliyun.oss.model.OSSObject;
import com.aliyun.oss.model.PartETag;
import com.aliyun.oss.model.PutObjectRequest;
import com.wkclz.core.exception.ValidationException;
import com.wkclz.micro.fileos.bean.FileosConstant;
import com.wkclz.micro.fileos.bean.dto.CompletedPartInfo;
import com.wkclz.micro.fileos.bean.dto.MdmFileosRecordDto;
import com.wkclz.micro.fileos.bean.dto.MultipartUploadInitResponse;
import com.wkclz.micro.fileos.bean.dto.PresignedPartInfo;
import com.wkclz.micro.fileos.bean.dto.PresignUploadResponse;
import com.wkclz.micro.fileos.bean.entity.MdmFileosBucket;
import com.wkclz.micro.fileos.service.FileosService;
import com.wkclz.micro.fileos.helper.PathHelper;
import com.wkclz.micro.fileos.utils.OssUtil;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service("AliOssService")
public class AliOssServiceImpl implements FileosService {

    private final ConcurrentHashMap<String, OSS> ossClientCache = new ConcurrentHashMap<>();

    @Autowired
    private PathHelper pathHelper;

    @Override
    public MdmFileosRecordDto upload(MultipartFile file, MdmFileosBucket bucket, String fileId, String category, Boolean isPublic) {
        return uploadCommon(file, bucket, fileId);
    }

    private MdmFileosRecordDto uploadCommon(MultipartFile file, MdmFileosBucket bucket, String fileId) {
        OSS ossClient = getOrCreateOssClient(bucket);
        String bucketName = bucket.getBucketName();
        String filename = file.getOriginalFilename();
        String contentType = OssUtil.getContentType(filename);

        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentType(contentType);
        metadata.setContentLength(file.getSize());
        if (fileId.startsWith(FileosConstant.PUBLIC_PREFIX)) {
            metadata.setObjectAcl(CannedAccessControlList.PublicRead);
        }
        try {
            PutObjectRequest putObjectRequest = new PutObjectRequest(bucketName, fileId, file.getInputStream(), metadata);
            ossClient.putObject(putObjectRequest);
            log.info("Upload file to Ali OSS success: fileId={}, bucketName={}", fileId, bucketName);
            String previewUrl = sign(fileId, bucket, 10, TimeUnit.MINUTES);

            MdmFileosRecordDto dto = new MdmFileosRecordDto();
            dto.setFileId(fileId);
            dto.setOssSp(bucket.getOssSp());
            dto.setBucketName(bucketName);
            dto.setPreviewUrl(previewUrl);
            return dto;
        } catch (OSSException oe) {
            log.error("Upload file to Ali OSS failed: errCode: {}, requestId: {}, hostId: {}, msg: {}",
                oe.getErrorCode(), oe.getRequestId(), oe.getHostId(), oe.getErrorMessage());
            throw ValidationException.of("文件上传失败: {}", oe.getErrorCode());
        } catch (ClientException ce) {
            log.error("Upload file to Ali OSS client failed: {}", ce.getMessage());
            throw ValidationException.of("文件上传失败，OSS客户端异常");
        } catch (IOException e) {
            log.error("Upload file to Ali OSS IO failed: {}", e.getMessage());
            throw ValidationException.of("文件上传失败: {}", e.getMessage());
        }
    }

    @Override
    public String sign(String fileId, MdmFileosBucket bucket, Integer expire, TimeUnit timeUnit) {
        List<String> signs = sign(Collections.singletonList(fileId), bucket, expire, timeUnit);
        return signs.get(0);
    }

    @Override
    public List<String> sign(List<String> fileIds, MdmFileosBucket bucket, Integer expire, TimeUnit timeUnit) {
        List<String> result = new ArrayList<>();
        String bucketName = bucket.getBucketName();
        String endpointOuter = bucket.getEndpointOuter();

        long millis = timeUnit.toMillis(expire);
        Date expireTime = Date.from(Instant.now().plusMillis(millis));

        OSS ossClient = getOrCreateOssClient(bucket);
        try {
            for (String fileId : fileIds) {
                if (fileId.startsWith("http")) {
                    URL url = new URL(fileId);
                    String newUrl = endpointOuter
                        + url.getPath()
                        + (url.getQuery() != null ? "?" + url.getQuery() : "")
                        + (url.getRef() != null ? "#" + url.getRef() : "");
                    result.add(newUrl);
                    continue;
                }
                if (fileId.startsWith(FileosConstant.PUBLIC_PREFIX)) {
                    result.add(endpointOuter + "/" + fileId);
                    continue;
                }

                URL url = ossClient.generatePresignedUrl(bucketName, fileId, expireTime);
                String newUrl = endpointOuter
                    + url.getPath()
                    + (url.getQuery() != null ? "?" + url.getQuery() : "")
                    + (url.getRef() != null ? "#" + url.getRef() : "");
                result.add(newUrl);
            }
        } catch (MalformedURLException e) {
            log.error("Sign URL parse failed: {}", e.getMessage());
            throw ValidationException.of("签名URL生成失败: {}", e.getMessage());
        }
        return result;
    }

    @Override
    public Integer delete(List<String> fileIds, MdmFileosBucket bucket) {
        String bucketName = bucket.getBucketName();
        OSS ossClient = getOrCreateOssClient(bucket);
        try {
            DeleteObjectsRequest request = new DeleteObjectsRequest(bucketName);
            request.setKeys(fileIds);
            DeleteObjectsResult objectsResult = ossClient.deleteObjects(request);
            List<String> objects = objectsResult.getDeletedObjects();
            log.info("Delete files from Ali OSS success: count={}, bucketName={}", objects.size(), bucketName);
            return objects.size();
        } catch (OSSException oe) {
            log.error("Delete file from Ali OSS failed: errCode: {}, requestId: {}, hostId: {}, msg: {}",
                oe.getErrorCode(), oe.getRequestId(), oe.getHostId(), oe.getErrorMessage());
            throw ValidationException.of("文件删除失败: {}", oe.getErrorCode());
        } catch (ClientException ce) {
            log.error("Delete file from Ali OSS client failed: {}", ce.getMessage());
            throw ValidationException.of("文件删除失败，OSS客户端异常");
        }
    }

    @Override
    public PresignUploadResponse presignUpload(String fileId, MdmFileosBucket bucket, String contentType, Integer expireMinutes) {
        OSS ossClient = getOrCreateOssClient(bucket);
        String bucketName = bucket.getBucketName();
        String endpointOuter = bucket.getEndpointOuter();

        Date expireTime = Date.from(Instant.now().plusSeconds(expireMinutes * 60L));
        GeneratePresignedUrlRequest request = new GeneratePresignedUrlRequest(bucketName, fileId, HttpMethod.PUT);
        request.setExpiration(expireTime);
        if (contentType != null) {
            request.setContentType(contentType);
        }

        try {
            URL url = ossClient.generatePresignedUrl(request);
            String presignUrl = endpointOuter
                + url.getPath()
                + (url.getQuery() != null ? "?" + url.getQuery() : "")
                + (url.getRef() != null ? "#" + url.getRef() : "");

            PresignUploadResponse response = new PresignUploadResponse();
            response.setFileId(fileId);
            response.setPresignUrl(presignUrl);
            response.setOssSp(bucket.getOssSp());
            response.setBucketName(bucketName);
            response.setContentType(contentType);
            response.setExpireMinutes(expireMinutes);
            return response;
        } catch (Exception e) {
            log.error("Generate presigned upload URL for Ali OSS failed: {}", e.getMessage());
            throw ValidationException.of("预签名上传URL生成失败: {}", e.getMessage());
        }
    }

    @Override
    public MultipartUploadInitResponse initMultipartUpload(String fileId, MdmFileosBucket bucket, String contentType, Integer partCount, Integer expireMinutes) {
        OSS ossClient = getOrCreateOssClient(bucket);
        String bucketName = bucket.getBucketName();
        String endpointOuter = bucket.getEndpointOuter();

        ObjectMetadata metadata = new ObjectMetadata();
        if (contentType != null) {
            metadata.setContentType(contentType);
        }
        if (fileId.startsWith(FileosConstant.PUBLIC_PREFIX)) {
            metadata.setObjectAcl(CannedAccessControlList.PublicRead);
        }

        InitiateMultipartUploadRequest initRequest = new InitiateMultipartUploadRequest(bucketName, fileId, metadata);
        InitiateMultipartUploadResult initResult;
        try {
            initResult = ossClient.initiateMultipartUpload(initRequest);
        } catch (OSSException oe) {
            log.error("Initiate multipart upload to Ali OSS failed: errCode: {}, msg: {}", oe.getErrorCode(), oe.getErrorMessage());
            throw ValidationException.of("初始化分片上传失败: {}", oe.getErrorCode());
        } catch (ClientException ce) {
            log.error("Initiate multipart upload to Ali OSS client failed: {}", ce.getMessage());
            throw ValidationException.of("初始化分片上传失败，OSS客户端异常");
        }

        String uploadId = initResult.getUploadId();
        log.info("Initiate multipart upload to Ali OSS success: fileId={}, uploadId={}, partCount={}", fileId, uploadId, partCount);

        Date expireTime = Date.from(Instant.now().plusSeconds(expireMinutes * 60L));
        List<PresignedPartInfo> parts = new ArrayList<>();
        for (int i = 1; i <= partCount; i++) {
            String presignUrl = buildPresignedUploadPartUrl(bucketName, fileId, uploadId, i, expireTime, bucket.getAccessKey(), bucket.getSecretKey(), endpointOuter);
            PresignedPartInfo partInfo = new PresignedPartInfo();
            partInfo.setPartNumber(i);
            partInfo.setPresignUrl(presignUrl);
            parts.add(partInfo);
        }

        MultipartUploadInitResponse response = new MultipartUploadInitResponse();
        response.setUploadId(uploadId);
        response.setFileId(fileId);
        response.setOssSp(bucket.getOssSp());
        response.setBucketName(bucketName);
        response.setContentType(contentType);
        response.setExpireMinutes(expireMinutes);
        response.setParts(parts);
        return response;
    }

    @Override
    public void completeMultipartUpload(String uploadId, String fileId, MdmFileosBucket bucket, List<CompletedPartInfo> parts) {
        OSS ossClient = getOrCreateOssClient(bucket);
        String bucketName = bucket.getBucketName();

        List<PartETag> partETags = parts.stream()
            .map(p -> new PartETag(p.getPartNumber(), p.getETag()))
            .toList();

        try {
            CompleteMultipartUploadRequest request = new CompleteMultipartUploadRequest(bucketName, fileId, uploadId, partETags);
            ossClient.completeMultipartUpload(request);
            log.info("Complete multipart upload to Ali OSS success: fileId={}, uploadId={}", fileId, uploadId);
        } catch (OSSException oe) {
            log.error("Complete multipart upload to Ali OSS failed: errCode: {}, msg: {}", oe.getErrorCode(), oe.getErrorMessage());
            throw ValidationException.of("完成分片上传失败: {}", oe.getErrorCode());
        } catch (ClientException ce) {
            log.error("Complete multipart upload to Ali OSS client failed: {}", ce.getMessage());
            throw ValidationException.of("完成分片上传失败，OSS客户端异常");
        }
    }

    @Override
    public void abortMultipartUpload(String uploadId, String fileId, MdmFileosBucket bucket) {
        OSS ossClient = getOrCreateOssClient(bucket);
        String bucketName = bucket.getBucketName();

        try {
            AbortMultipartUploadRequest request = new AbortMultipartUploadRequest(bucketName, fileId, uploadId);
            ossClient.abortMultipartUpload(request);
            log.info("Abort multipart upload to Ali OSS success: fileId={}, uploadId={}", fileId, uploadId);
        } catch (OSSException oe) {
            log.error("Abort multipart upload to Ali OSS failed: errCode: {}, msg: {}", oe.getErrorCode(), oe.getErrorMessage());
            throw ValidationException.of("中止分片上传失败: {}", oe.getErrorCode());
        } catch (ClientException ce) {
            log.error("Abort multipart upload to Ali OSS client failed: {}", ce.getMessage());
            throw ValidationException.of("中止分片上传失败，OSS客户端异常");
        }
    }

    @Override
    public InputStream download(String fileId, MdmFileosBucket bucket) {
        OSS ossClient = getOrCreateOssClient(bucket);
        String bucketName = bucket.getBucketName();
        try {
            OSSObject ossObject = ossClient.getObject(bucketName, fileId);
            return ossObject.getObjectContent();
        } catch (OSSException oe) {
            log.error("Download file from Ali OSS failed: errCode: {}, msg: {}", oe.getErrorCode(), oe.getErrorMessage());
            throw ValidationException.of("文件下载失败: {}", oe.getErrorCode());
        } catch (ClientException ce) {
            log.error("Download file from Ali OSS client failed: {}", ce.getMessage());
            throw ValidationException.of("文件下载失败，OSS客户端异常");
        }
    }

    @Override
    public InputStream download(String fileId, MdmFileosBucket bucket, long offset, long length) {
        OSS ossClient = getOrCreateOssClient(bucket);
        String bucketName = bucket.getBucketName();
        try {
            GetObjectRequest getObjectRequest = new GetObjectRequest(bucketName, fileId);
            getObjectRequest.setRange(offset, offset + length - 1);
            OSSObject ossObject = ossClient.getObject(getObjectRequest);
            return ossObject.getObjectContent();
        } catch (OSSException oe) {
            log.error("Range download file from Ali OSS failed: errCode: {}, msg: {}", oe.getErrorCode(), oe.getErrorMessage());
            throw ValidationException.of("文件下载失败: {}", oe.getErrorCode());
        } catch (ClientException ce) {
            log.error("Range download file from Ali OSS client failed: {}", ce.getMessage());
            throw ValidationException.of("文件下载失败，OSS客户端异常");
        }
    }

    private String buildPresignedUploadPartUrl(String bucketName, String fileId, String uploadId, int partNumber, Date expireTime, String accessKey, String secretKey, String endpointOuter) {
        String expires = String.valueOf(expireTime.getTime() / 1000);
        String canonicalizedResource = "/" + bucketName + "/" + fileId + "?partNumber=" + partNumber + "&uploadId=" + uploadId;
        String stringToSign = "PUT\n\n\n" + expires + "\n" + canonicalizedResource;

        String signature;
        try {
            SecretKeySpec keySpec = new SecretKeySpec(secretKey.getBytes(StandardCharsets.UTF_8), "HmacSHA1");
            Mac mac = Mac.getInstance("HmacSHA1");
            mac.init(keySpec);
            byte[] signData = mac.doFinal(stringToSign.getBytes(StandardCharsets.UTF_8));
            signature = URLEncoder.encode(Base64.getEncoder().encodeToString(signData), StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw ValidationException.of("签名计算失败: {}", e.getMessage());
        }

        if (!endpointOuter.startsWith("http")) {
            endpointOuter = "https://" + endpointOuter;
        }
        return endpointOuter + "/" + fileId
            + "?OSSAccessKeyId=" + accessKey
            + "&Expires=" + expires
            + "&Signature=" + signature
            + "&partNumber=" + partNumber
            + "&uploadId=" + uploadId;
    }

    private OSS getOrCreateOssClient(MdmFileosBucket bucket) {
        return ossClientCache.computeIfAbsent(bucket.getBucketName(), k -> createOssClient(bucket));
    }

    private static OSS createOssClient(MdmFileosBucket bucket) {
        String endpointInner = bucket.getEndpointInner();
        String accessKey = bucket.getAccessKey();
        String secretKey = bucket.getSecretKey();
        if (!endpointInner.startsWith("http")) {
            endpointInner = "https://" + endpointInner;
        }
        DefaultCredentialProvider credentialProvider = CredentialsProviderFactory.newDefaultCredentialProvider(accessKey, secretKey);
        return new OSSClientBuilder().build(endpointInner, credentialProvider);
    }

    @PreDestroy
    public void destroy() {
        ossClientCache.values().forEach(OSS::shutdown);
        ossClientCache.clear();
    }

}
