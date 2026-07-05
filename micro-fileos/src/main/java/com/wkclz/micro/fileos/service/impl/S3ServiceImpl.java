package com.wkclz.micro.fileos.service.impl;

import com.wkclz.core.exception.ValidationException;
import com.wkclz.micro.fileos.bean.FileosConstant;
import com.wkclz.micro.fileos.bean.dto.CompletedPartInfo;
import com.wkclz.micro.fileos.bean.dto.PresignedPartInfo;
import com.wkclz.micro.fileos.bean.entity.MdmFileosBucket;
import com.wkclz.micro.fileos.bean.resp.MultipartUploadInitResp;
import com.wkclz.micro.fileos.bean.resp.PresignUploadResp;
import com.wkclz.micro.fileos.bean.resp.RecordResp;
import com.wkclz.micro.fileos.service.FileosService;
import com.wkclz.micro.fileos.helper.PathHelper;
import com.wkclz.micro.fileos.utils.OssUtil;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.AbortMultipartUploadRequest;
import software.amazon.awssdk.services.s3.model.CompletedPart;
import software.amazon.awssdk.services.s3.model.CompleteMultipartUploadRequest;
import software.amazon.awssdk.services.s3.model.CreateMultipartUploadRequest;
import software.amazon.awssdk.services.s3.model.CreateMultipartUploadResponse;
import software.amazon.awssdk.services.s3.model.Delete;
import software.amazon.awssdk.services.s3.model.DeleteObjectsRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.ObjectCannedACL;
import software.amazon.awssdk.services.s3.model.ObjectIdentifier;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;
import software.amazon.awssdk.services.s3.model.UploadPartRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedUploadPartRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.UploadPartPresignRequest;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service("S3Service")
public class S3ServiceImpl implements FileosService {

    private final ConcurrentHashMap<String, S3Client> s3ClientCache = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, S3Presigner> s3PresignerCache = new ConcurrentHashMap<>();

    @Autowired
    private PathHelper pathHelper;

    @Override
    public RecordResp upload(MultipartFile file, MdmFileosBucket bucket, String fileId, String category, Boolean isPublic) {
        return uploadCommon(file, bucket, fileId);
    }

    private RecordResp uploadCommon(MultipartFile file, MdmFileosBucket bucket, String fileId) {
        S3Client s3 = getOrCreateS3Client(bucket);
        String bucketName = bucket.getBucketName();
        String filename = file.getOriginalFilename();
        String contentType = OssUtil.getContentType(filename);
        try {
            PutObjectRequest request = PutObjectRequest
                .builder()
                .key(fileId)
                .bucket(bucketName)
                .contentType(contentType)
                .contentLength(file.getSize())
                .acl(fileId.startsWith(FileosConstant.PUBLIC_PREFIX) ? ObjectCannedACL.PUBLIC_READ : ObjectCannedACL.AUTHENTICATED_READ)
                .build();
            RequestBody requestBody = RequestBody.fromInputStream(file.getInputStream(), file.getSize());
            s3.putObject(request, requestBody);
            log.info("Upload file to S3 success: fileId={}, bucketName={}", fileId, bucketName);
            String previewUrl = sign(fileId, bucket, 10, TimeUnit.MINUTES);

            RecordResp resp = new RecordResp();
            resp.setFileId(fileId);
            resp.setOssSp(bucket.getOssSp());
            resp.setBucketName(bucketName);
            resp.setPreviewUrl(previewUrl);
            return resp;
        } catch (IOException e) {
            log.error("Upload file [{}] to AWS S3 IO failed: {}", fileId, e.getMessage());
            throw ValidationException.of("文件上传失败: {}", e.getMessage());
        } catch (S3Exception e) {
            log.error("Upload file [{}] to AWS S3 failed: {}", fileId, e.awsErrorDetails().errorCode());
            throw ValidationException.of("文件上传失败: {}", e.awsErrorDetails().errorCode());
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
        String endpointOuter = bucket.getEndpointOuter();

        long millis = timeUnit.toMillis(expire);
        S3Presigner presigner = getOrCreateS3Presigner(bucket);

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
                GetObjectPresignRequest getObjectPresignRequest = GetObjectPresignRequest.builder()
                    .getObjectRequest(builder -> builder
                        .bucket(bucket.getBucketName())
                        .key(fileId))
                    .signatureDuration(Duration.ofMillis(millis))
                    .build();
                PresignedGetObjectRequest presignedRequest = presigner.presignGetObject(getObjectPresignRequest);
                URL url = presignedRequest.url();
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
        S3Client s3 = getOrCreateS3Client(bucket);
        try {
            List<ObjectIdentifier> objectIdentifiers = fileIds.stream()
                .map(key -> ObjectIdentifier.builder().key(key).build())
                .toList();

            Delete delete = Delete.builder()
                .objects(objectIdentifiers)
                .build();

            var deleteObjectsRequest = DeleteObjectsRequest.builder()
                .bucket(bucket.getBucketName())
                .delete(delete)
                .build();

            s3.deleteObjects(deleteObjectsRequest);
            log.info("Delete files from S3 success: count={}, bucketName={}", fileIds.size(), bucket.getBucketName());
            return fileIds.size();
        } catch (S3Exception e) {
            log.error("Delete files from AWS S3 failed: {}", e.awsErrorDetails().errorCode());
            throw ValidationException.of("文件删除失败: {}", e.awsErrorDetails().errorCode());
        }
    }

    @Override
    public PresignUploadResp presignUpload(String fileId, MdmFileosBucket bucket, String contentType, Integer expireMinutes) {
        String bucketName = bucket.getBucketName();
        String endpointOuter = bucket.getEndpointOuter();
        S3Presigner presigner = getOrCreateS3Presigner(bucket);

        PutObjectPresignRequest presignRequest = PutObjectPresignRequest.builder()
            .signatureDuration(Duration.ofMinutes(expireMinutes))
            .putObjectRequest(builder -> {
                builder.bucket(bucketName).key(fileId);
                if (contentType != null) {
                    builder.contentType(contentType);
                }
            })
            .build();

        try {
            PresignedPutObjectRequest presignedRequest = presigner.presignPutObject(presignRequest);
            URL url = presignedRequest.url();
            String presignUrl = endpointOuter
                + url.getPath()
                + (url.getQuery() != null ? "?" + url.getQuery() : "")
                + (url.getRef() != null ? "#" + url.getRef() : "");

            PresignUploadResp resp = new PresignUploadResp();
            resp.setFileId(fileId);
            resp.setPresignUrl(presignUrl);
            resp.setOssSp(bucket.getOssSp());
            resp.setBucketName(bucketName);
            resp.setContentType(contentType);
            resp.setExpireMinutes(expireMinutes);
            return resp;
        } catch (Exception e) {
            log.error("Generate presigned upload URL for S3 failed: {}", e.getMessage());
            throw ValidationException.of("预签名上传URL生成失败: {}", e.getMessage());
        }
    }

    @Override
    public MultipartUploadInitResp initMultipartUpload(String fileId, MdmFileosBucket bucket, String contentType, Integer partCount, Integer expireMinutes) {
        S3Client s3 = getOrCreateS3Client(bucket);
        String bucketName = bucket.getBucketName();
        String endpointOuter = bucket.getEndpointOuter();

        CreateMultipartUploadRequest.Builder createRequestBuilder = CreateMultipartUploadRequest.builder()
            .bucket(bucketName)
            .key(fileId);
        if (contentType != null) {
            createRequestBuilder.contentType(contentType);
        }

        CreateMultipartUploadResponse createResponse;
        try {
            createResponse = s3.createMultipartUpload(createRequestBuilder.build());
        } catch (S3Exception e) {
            log.error("Initiate multipart upload to S3 failed: {}", e.awsErrorDetails().errorCode());
            throw ValidationException.of("初始化分片上传失败: {}", e.awsErrorDetails().errorCode());
        }

        String uploadId = createResponse.uploadId();
        log.info("Initiate multipart upload to S3 success: fileId={}, uploadId={}, partCount={}", fileId, uploadId, partCount);

        S3Presigner presigner = getOrCreateS3Presigner(bucket);
        List<PresignedPartInfo> parts = new ArrayList<>();
        for (int i = 1; i <= partCount; i++) {
            UploadPartPresignRequest partPresignRequest = UploadPartPresignRequest.builder()
                .signatureDuration(Duration.ofMinutes(expireMinutes))
                .uploadPartRequest(UploadPartRequest.builder()
                    .bucket(bucketName)
                    .key(fileId)
                    .uploadId(uploadId)
                    .partNumber(i)
                    .build())
                .build();

            PresignedUploadPartRequest presignedPart = presigner.presignUploadPart(partPresignRequest);
            URL partUrl = presignedPart.url();
            String presignUrl = endpointOuter
                + partUrl.getPath()
                + (partUrl.getQuery() != null ? "?" + partUrl.getQuery() : "")
                + (partUrl.getRef() != null ? "#" + partUrl.getRef() : "");

            PresignedPartInfo partInfo = new PresignedPartInfo();
            partInfo.setPartNumber(i);
            partInfo.setPresignUrl(presignUrl);
            parts.add(partInfo);
        }

        MultipartUploadInitResp resp = new MultipartUploadInitResp();
        resp.setUploadId(uploadId);
        resp.setFileId(fileId);
        resp.setOssSp(bucket.getOssSp());
        resp.setBucketName(bucketName);
        resp.setContentType(contentType);
        resp.setExpireMinutes(expireMinutes);
        resp.setParts(parts);
        return resp;
    }

    @Override
    public void completeMultipartUpload(String uploadId, String fileId, MdmFileosBucket bucket, List<CompletedPartInfo> parts) {
        S3Client s3 = getOrCreateS3Client(bucket);
        String bucketName = bucket.getBucketName();

        List<CompletedPart> completedParts = parts.stream()
            .map(p -> CompletedPart.builder()
                .partNumber(p.getPartNumber())
                .eTag(p.getETag())
                .build())
            .toList();

        try {
            CompleteMultipartUploadRequest request = CompleteMultipartUploadRequest.builder()
                .bucket(bucketName)
                .key(fileId)
                .uploadId(uploadId)
                .multipartUpload(multipart -> multipart.parts(completedParts))
                .build();
            s3.completeMultipartUpload(request);
            log.info("Complete multipart upload to S3 success: fileId={}, uploadId={}", fileId, uploadId);
        } catch (S3Exception e) {
            log.error("Complete multipart upload to S3 failed: {}", e.awsErrorDetails().errorCode());
            throw ValidationException.of("完成分片上传失败: {}", e.awsErrorDetails().errorCode());
        }
    }

    @Override
    public void abortMultipartUpload(String uploadId, String fileId, MdmFileosBucket bucket) {
        S3Client s3 = getOrCreateS3Client(bucket);
        String bucketName = bucket.getBucketName();

        try {
            AbortMultipartUploadRequest request = AbortMultipartUploadRequest.builder()
                .bucket(bucketName)
                .key(fileId)
                .uploadId(uploadId)
                .build();
            s3.abortMultipartUpload(request);
            log.info("Abort multipart upload to S3 success: fileId={}, uploadId={}", fileId, uploadId);
        } catch (S3Exception e) {
            log.error("Abort multipart upload to S3 failed: {}", e.awsErrorDetails().errorCode());
            throw ValidationException.of("中止分片上传失败: {}", e.awsErrorDetails().errorCode());
        }
    }

    @Override
    public InputStream download(String fileId, MdmFileosBucket bucket) {
        S3Client s3 = getOrCreateS3Client(bucket);
        try {
            GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(bucket.getBucketName())
                .key(fileId)
                .build();
            return s3.getObject(getObjectRequest);
        } catch (S3Exception e) {
            log.error("Download file from S3 failed: {}", e.awsErrorDetails().errorCode());
            throw ValidationException.of("文件下载失败: {}", e.awsErrorDetails().errorCode());
        }
    }

    @Override
    public InputStream download(String fileId, MdmFileosBucket bucket, long offset, long length) {
        S3Client s3 = getOrCreateS3Client(bucket);
        try {
            GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(bucket.getBucketName())
                .key(fileId)
                .range("bytes=" + offset + "-" + (offset + length - 1))
                .build();
            return s3.getObject(getObjectRequest);
        } catch (S3Exception e) {
            log.error("Range download file from S3 failed: {}", e.awsErrorDetails().errorCode());
            throw ValidationException.of("文件下载失败: {}", e.awsErrorDetails().errorCode());
        }
    }

    private S3Client getOrCreateS3Client(MdmFileosBucket bucket) {
        return s3ClientCache.computeIfAbsent(bucket.getBucketName(), k -> createS3Client(bucket));
    }

    private static S3Client createS3Client(MdmFileosBucket bucket) {
        String endpointInner = bucket.getEndpointInner();
        String region = bucket.getRegion();
        if (!endpointInner.startsWith("http")) {
            endpointInner = "https://" + endpointInner;
        }
        StaticCredentialsProvider credentialsProvider = getCredentialsProvider(bucket);
        return S3Client.builder()
            .region(Region.of(region))
            .endpointOverride(URI.create(endpointInner))
            .credentialsProvider(credentialsProvider)
            .build();
    }

    private S3Presigner getOrCreateS3Presigner(MdmFileosBucket bucket) {
        return s3PresignerCache.computeIfAbsent(bucket.getBucketName(), k -> createS3Presigner(bucket));
    }

    private static S3Presigner createS3Presigner(MdmFileosBucket bucket) {
        String endpointInner = bucket.getEndpointInner();
        String region = bucket.getRegion();
        if (!endpointInner.startsWith("http")) {
            endpointInner = "https://" + endpointInner;
        }
        StaticCredentialsProvider credentialsProvider = getCredentialsProvider(bucket);
        return S3Presigner.builder()
            .region(Region.of(region))
            .endpointOverride(URI.create(endpointInner))
            .credentialsProvider(credentialsProvider)
            .build();
    }

    private static StaticCredentialsProvider getCredentialsProvider(MdmFileosBucket bucket) {
        String accessKey = bucket.getAccessKey();
        String secretKey = bucket.getSecretKey();
        AwsBasicCredentials awsBasicCredentials = AwsBasicCredentials.create(accessKey, secretKey);
        return StaticCredentialsProvider.create(awsBasicCredentials);
    }

    @PreDestroy
    public void destroy() {
        s3PresignerCache.values().forEach(S3Presigner::close);
        s3PresignerCache.clear();
        s3ClientCache.values().forEach(S3Client::close);
        s3ClientCache.clear();
    }

}
