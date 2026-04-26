package com.wkclz.micro.file.service.impl;

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
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.Delete;
import software.amazon.awssdk.services.s3.model.DeleteObjectsRequest;
import software.amazon.awssdk.services.s3.model.ObjectCannedACL;
import software.amazon.awssdk.services.s3.model.ObjectIdentifier;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;

import java.io.IOException;
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
public class S3ServiceImpl implements FileService {

    private final ConcurrentHashMap<String, S3Client> s3ClientCache = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, S3Presigner> s3PresignerCache = new ConcurrentHashMap<>();

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
        S3Client s3 = getOrCreateS3Client(fsBucket);
        String bucket = fsBucket.getBucket();
        String filename = file.getOriginalFilename();
        String contentType = OssUtil.getContentType(filename);
        try {
            PutObjectRequest request = PutObjectRequest
                .builder()
                .key(fileId)
                .bucket(bucket)
                .contentType(contentType)
                .contentLength(file.getSize())
                .acl(fileId.startsWith(FileConstant.PUBLIC_PREFIX) ? ObjectCannedACL.PUBLIC_READ : ObjectCannedACL.AUTHENTICATED_READ)
                .build();
            RequestBody requestBody = RequestBody.fromInputStream(file.getInputStream(), file.getSize());
            s3.putObject(request, requestBody);
            String previewUrl = sign(fileId, fsBucket, 10, TimeUnit.MINUTES);

            MdmFileRecordDto dto = new MdmFileRecordDto();
            dto.setFileId(fileId);
            dto.setOssSp(fsBucket.getOssSp());
            dto.setBucket(bucket);
            dto.setPreviewUrl(previewUrl);
            return dto;
        } catch (IOException e) {
            log.error("Upload file [{}] to AWS S3 IO failed: {}", fileId, e.getMessage());
            throw ValidationException.of("文件上传失败: {}", e.getMessage());
        } catch (S3Exception e) {
            log.error("Upload file [{}] to AWS S3 failed: {}", fileId, e.awsErrorDetails().errorCode());
            throw ValidationException.of("文件上传失败: {}", e.awsErrorDetails().errorCode());
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
        String endpointOuter = fsBucket.getEndpointOuter();

        long millis = timeUnit.toMillis(expire);
        S3Presigner presigner = getOrCreateS3Presigner(fsBucket);

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
                if (fileId.startsWith(FileConstant.PUBLIC_PREFIX)) {
                    result.add(endpointOuter + "/" + fileId);
                    continue;
                }
                GetObjectPresignRequest getObjectPresignRequest = GetObjectPresignRequest.builder()
                    .getObjectRequest(builder -> builder
                        .bucket(fsBucket.getBucket())
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

    /**
     * 多文件删除
     */
    @Override
    public Integer delete(List<String> fileIds, MdmFileBucket fsBucket) {
        S3Client s3 = getOrCreateS3Client(fsBucket);
        try {
            List<ObjectIdentifier> objectIdentifiers = fileIds.stream()
                .map(key -> ObjectIdentifier.builder().key(key).build())
                .toList();

            Delete delete = Delete.builder()
                .objects(objectIdentifiers)
                .build();

            var deleteObjectsRequest = DeleteObjectsRequest.builder()
                .bucket(fsBucket.getBucket())
                .delete(delete)
                .build();

            s3.deleteObjects(deleteObjectsRequest);
            return fileIds.size();
        } catch (S3Exception e) {
            log.error("Delete files from AWS S3 failed: {}", e.awsErrorDetails().errorCode());
            throw ValidationException.of("文件删除失败: {}", e.awsErrorDetails().errorCode());
        }
    }

    private S3Client getOrCreateS3Client(MdmFileBucket fsBucket) {
        return s3ClientCache.computeIfAbsent(fsBucket.getBucket(), k -> createS3Client(fsBucket));
    }

    private static S3Client createS3Client(MdmFileBucket fsBucket) {
        String endpointInner = fsBucket.getEndpointInner();
        String region = fsBucket.getRegion();
        if (!endpointInner.startsWith("http")) {
            endpointInner = "https://" + endpointInner;
        }

        // 通过 accessKey、secretKey 生成认证的aws凭证对象
        StaticCredentialsProvider credentialsProvider = getCredentialsProvider(fsBucket);
        return S3Client.builder()
            .region(Region.of(region))
            .endpointOverride(URI.create(endpointInner))
            .credentialsProvider(credentialsProvider)
            .build();
    }

    private S3Presigner getOrCreateS3Presigner(MdmFileBucket fsBucket) {
        return s3PresignerCache.computeIfAbsent(fsBucket.getBucket(), k -> createS3Presigner(fsBucket));
    }

    private static S3Presigner createS3Presigner(MdmFileBucket fsBucket) {
        String endpointInner = fsBucket.getEndpointInner();
        String region = fsBucket.getRegion();
        if (!endpointInner.startsWith("http")) {
            endpointInner = "https://" + endpointInner;
        }
        StaticCredentialsProvider credentialsProvider = getCredentialsProvider(fsBucket);
        return S3Presigner.builder()
            .region(Region.of(region))
            .endpointOverride(URI.create(endpointInner))
            .credentialsProvider(credentialsProvider)
            .build();
    }

    private static StaticCredentialsProvider getCredentialsProvider(MdmFileBucket fsBucket) {
        String accessKey = fsBucket.getAccessKey();
        String secretKey = fsBucket.getSecretKey();
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
