package com.wkclz.micro.file.service.impl;


import com.wkclz.micro.file.pojo.FsConstant;
import com.wkclz.micro.file.pojo.dto.MdmFsFilesDto;
import com.wkclz.micro.file.pojo.entity.MdmFsBucket;
import com.wkclz.micro.file.service.FileService;
import com.wkclz.micro.file.utils.OssUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;
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
import java.util.concurrent.TimeUnit;

@Slf4j
@Service("S3Service")
public class S3ServiceImpl implements FileService {

    @Override
    public MdmFsFilesDto upload(MultipartFile file, MdmFsBucket fsBucket, String businessType) {
        String filename = file.getOriginalFilename();
        String fileId = OssUtil.getFullName(businessType, filename);
        return uploadCommon(file, fsBucket, fileId);
    }

    @Override
    public MdmFsFilesDto uploadPublic(MultipartFile file, MdmFsBucket fsBucket, String businessType) {
        String filename = file.getOriginalFilename();
        String fileId = FsConstant.PUBLIC_PREFIX + OssUtil.getFullName(businessType, filename);
        return uploadCommon(file, fsBucket, fileId);
    }

    private MdmFsFilesDto uploadCommon(MultipartFile file, MdmFsBucket fsBucket, String fileId) {
        S3Client s3 = getS3Client(fsBucket);
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
                .acl(fileId.startsWith(FsConstant.PUBLIC_PREFIX) ? ObjectCannedACL.PUBLIC_READ :ObjectCannedACL.AUTHENTICATED_READ)
                .build();
            RequestBody requestBody = RequestBody.fromInputStream(file.getInputStream(), file.getSize());
            s3.putObject(request, requestBody);

            String previewUrl = sign(fileId, fsBucket, 10, TimeUnit.MINUTES, s3);

            MdmFsFilesDto dto = new MdmFsFilesDto();
            dto.setFileId(fileId);
            dto.setOssSp(fsBucket.getOssSp());
            dto.setBucket(bucket);
            dto.setPreviewUrl(previewUrl);
            return dto;
        } catch (IOException e) {
            log.error(String.format("Upload file [%s] to AWS S3 failed! Error: %s", fileId, e.getMessage()));
            throw new RuntimeException(e);
        } finally {
            s3.close();
        }
    }


    @Override
    public String sign(String fileId, MdmFsBucket fsBucket, Integer expire, TimeUnit timeUnit) {
        List<String> signs = sign(Collections.singletonList(fileId), fsBucket, expire, timeUnit, null);
        return signs.get(0);
    }
    @Override
    public List<String> sign(List<String> fileIds, MdmFsBucket fsBucket, Integer expire, TimeUnit timeUnit) {
        return sign(fileIds, fsBucket, expire, timeUnit, null);
    }

    private String sign(String fileId, MdmFsBucket fsBucket, Integer expire, TimeUnit timeUnit, S3Client s3) {
        List<String> signs = sign(Collections.singletonList(fileId), fsBucket, expire, timeUnit, s3);
        return signs.get(0);
    }

    private List<String> sign(List<String> fileIds, MdmFsBucket fsBucket, Integer expire, TimeUnit timeUnit, S3Client s3) {
        List<String> result = new ArrayList<>();
        String endpointInner = fsBucket.getEndpointInner();
        String endpointOuter = fsBucket.getEndpointOuter();
        long millis = timeUnit.toMillis(expire);

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
                if (fileId.startsWith(FsConstant.PUBLIC_PREFIX)) {
                    result.add(endpointOuter + "/" + fileId);
                    continue;
                }

                S3Presigner presigner = S3Presigner.builder()
                    .region(Region.of(fsBucket.getRegion()))
                    .credentialsProvider(getCredentialsProvider(fsBucket))
                    .build();

                GetObjectPresignRequest getObjectPresignRequest = GetObjectPresignRequest.builder()
                    .getObjectRequest(builder -> builder
                        .bucket(fsBucket.getBucket())
                        .key(fileId))
                    .signatureDuration(Duration.ofMillis(millis))
                    .build();

                // 生成预签名请求
                PresignedGetObjectRequest presignedRequest = presigner.presignGetObject(getObjectPresignRequest);
                URL url = presignedRequest.url();
                String newUrl = endpointOuter
                    + url.getPath()
                    + (url.getQuery() != null ? "?" + url.getQuery() : "")
                    + (url.getRef() != null ? "#" + url.getRef() : "");
                result.add(newUrl);
            }
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
        return result;
    }

    /**
     * 多文件删除
     */
    @Override
    public Integer delete(List<String> fileIds, MdmFsBucket fsBucket) {
        S3Client s3 = getS3Client(fsBucket);
        try {
            // 构建 ObjectIdentifier 列表
            List<ObjectIdentifier> objectIdentifiers = fileIds.stream()
                .map(key -> ObjectIdentifier.builder().key(key).build())
                .toList();

            // 创建 Delete 请求
            Delete delete = Delete.builder()
                .objects(objectIdentifiers)
                .build();

            // 创建 DeleteObjectsRequest
            var deleteObjectsRequest = DeleteObjectsRequest.builder()
                .bucket(fsBucket.getBucket())
                .delete(delete)
                .build();

            // 执行批量删除
            s3.deleteObjects(deleteObjectsRequest);
            return fileIds.size();
        } finally {
            s3.close();
        }
    }


    private S3Client getS3Client(MdmFsBucket fsBucket) {
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

    private static StaticCredentialsProvider getCredentialsProvider(MdmFsBucket fsBucket) {
        String accessKey = fsBucket.getAccessKey();
        String secretKey = fsBucket.getSecretKey();
        AwsBasicCredentials awsBasicCredentials = AwsBasicCredentials.create(accessKey, secretKey);
        return StaticCredentialsProvider.create(awsBasicCredentials);
    }

}
