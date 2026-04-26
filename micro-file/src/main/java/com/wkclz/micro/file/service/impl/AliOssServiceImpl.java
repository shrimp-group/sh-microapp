package com.wkclz.micro.file.service.impl;

import com.aliyun.oss.ClientException;
import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.OSSException;
import com.aliyun.oss.common.auth.CredentialsProviderFactory;
import com.aliyun.oss.common.auth.DefaultCredentialProvider;
import com.aliyun.oss.model.CannedAccessControlList;
import com.aliyun.oss.model.DeleteObjectsRequest;
import com.aliyun.oss.model.DeleteObjectsResult;
import com.aliyun.oss.model.ObjectMetadata;
import com.aliyun.oss.model.PutObjectRequest;
import com.wkclz.core.exception.ValidationException;
import com.wkclz.micro.file.bean.FileConstant;
import com.wkclz.micro.file.bean.dto.MdmFileRecordDto;
import com.wkclz.micro.file.bean.entity.MdmFileBucket;
import com.wkclz.micro.file.service.FileService;
import com.wkclz.micro.file.utils.OssUtil;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service("AliOssService")
public class AliOssServiceImpl implements FileService {

    private final ConcurrentHashMap<String, OSS> ossClientCache = new ConcurrentHashMap<>();

    @Override
    public MdmFileRecordDto upload(MultipartFile file, MdmFileBucket fsBucket, String businessType) {
        String filename = file.getOriginalFilename();
        String fileId = OssUtil.getFullName(businessType, filename);
        return uploadCommon(file, fsBucket, fileId);
    }

    @Override
    public MdmFileRecordDto uploadPublic(MultipartFile file, MdmFileBucket fsBucket, String businessType) {
        String filename = file.getOriginalFilename();
        String fileId = FileConstant.PUBLIC_PREFIX + OssUtil.getFullName(businessType, filename);
        return uploadCommon(file, fsBucket, fileId);
    }

    private MdmFileRecordDto uploadCommon(MultipartFile file, MdmFileBucket fsBucket, String fileId) {
        OSS ossClient = getOrCreateOssClient(fsBucket);
        String bucket = fsBucket.getBucket();
        String filename = file.getOriginalFilename();
        String contentType = OssUtil.getContentType(filename);

        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentType(contentType);
        metadata.setContentLength(file.getSize());
        if (fileId.startsWith(FileConstant.PUBLIC_PREFIX)) {
            metadata.setObjectAcl(CannedAccessControlList.PublicRead);
        }
        try {
            PutObjectRequest putObjectRequest = new PutObjectRequest(bucket, fileId, file.getInputStream(), metadata);
            ossClient.putObject(putObjectRequest);
            String previewUrl = sign(fileId, fsBucket, 10, TimeUnit.MINUTES);

            MdmFileRecordDto dto = new MdmFileRecordDto();
            dto.setFileId(fileId);
            dto.setOssSp(fsBucket.getOssSp());
            dto.setBucket(bucket);
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
    public String sign(String fileId, MdmFileBucket fsBucket, Integer expire, TimeUnit timeUnit) {
        List<String> signs = sign(Collections.singletonList(fileId), fsBucket, expire, timeUnit);
        return signs.get(0);
    }

    @Override
    public List<String> sign(List<String> fileIds, MdmFileBucket fsBucket, Integer expire, TimeUnit timeUnit) {
        List<String> result = new ArrayList<>();
        String bucket = fsBucket.getBucket();
        String endpointOuter = fsBucket.getEndpointOuter();

        long millis = timeUnit.toMillis(expire);
        Date expireTime = Date.from(Instant.now().plusMillis(millis));

        OSS ossClient = getOrCreateOssClient(fsBucket);
        try {
            for (String fileId : fileIds) {
                if (fileId.startsWith("http")) {
                    // 更换域名为配置的最新配置的域名
                    URL url = new URL(fileId);
                    String newUrl = endpointOuter
                        + url.getPath()
                        + (url.getQuery() != null ? "?" + url.getQuery() : "")
                        + (url.getRef() != null ? "#" + url.getRef() : "");
                    result.add(newUrl);
                    continue;
                }
                if (fileId.startsWith(FileConstant.PUBLIC_PREFIX)) {
                    // 为开放访问的 file, 直接加上域名即可
                    result.add(endpointOuter + "/" + fileId);
                    continue;
                }

                // 签名访问 【域名要更换】
                URL url = ossClient.generatePresignedUrl(bucket, fileId, expireTime);
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


    /**
     * OSS 多文件删除
     */
    @Override
    public Integer delete(List<String> fileIds, MdmFileBucket fsBucket) {
        String bucket = fsBucket.getBucket();
        OSS ossClient = getOrCreateOssClient(fsBucket);
        try {
            // 删除文件
            DeleteObjectsRequest request = new DeleteObjectsRequest(bucket);
            request.setKeys(fileIds);
            DeleteObjectsResult objectsResult = ossClient.deleteObjects(request);
            List<String> objects = objectsResult.getDeletedObjects();
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

    private OSS getOrCreateOssClient(MdmFileBucket fsBucket) {
        return ossClientCache.computeIfAbsent(fsBucket.getBucket(), k -> createOssClient(fsBucket));
    }

    private static OSS createOssClient(MdmFileBucket fsBucket) {
        String endpointInner = fsBucket.getEndpointInner();
        String accessKey = fsBucket.getAccessKey();
        String secretKey = fsBucket.getSecretKey();
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
