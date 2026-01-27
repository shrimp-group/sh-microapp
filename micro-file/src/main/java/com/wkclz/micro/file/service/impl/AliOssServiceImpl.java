package com.wkclz.micro.file.service.impl;

import com.aliyun.oss.ClientException;
import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.OSSException;
import com.aliyun.oss.common.auth.CredentialsProviderFactory;
import com.aliyun.oss.common.auth.DefaultCredentialProvider;
import com.aliyun.oss.model.DeleteObjectsRequest;
import com.aliyun.oss.model.DeleteObjectsResult;
import com.aliyun.oss.model.ObjectMetadata;
import com.aliyun.oss.model.PutObjectRequest;
import com.wkclz.micro.file.pojo.FsConstant;
import com.wkclz.micro.file.pojo.dto.MdmFsFilesDto;
import com.wkclz.micro.file.pojo.entity.MdmFsBucket;
import com.wkclz.micro.file.service.FileService;
import com.wkclz.micro.file.utils.OssUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service("AliOssService")
public class AliOssServiceImpl implements FileService {

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
        OSS ossClient = getOssClient(fsBucket);
        String bucket = fsBucket.getBucket();
        String filename = file.getOriginalFilename();
        String contentType = OssUtil.getContentType(filename);

        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentType(contentType);
        metadata.setContentLength(file.getSize());
        if (fileId.startsWith(FsConstant.PUBLIC_PREFIX)) {
            metadata.setObjectAcl(com.aliyun.oss.model.CannedAccessControlList.PublicRead);
        }
        try {
            PutObjectRequest putObjectRequest = new PutObjectRequest(bucket, fileId, file.getInputStream(), metadata);
            ossClient.putObject(putObjectRequest);
            String previewUrl = sign(fileId, fsBucket, 10, TimeUnit.MINUTES, ossClient);

            MdmFsFilesDto dto = new MdmFsFilesDto();
            dto.setFileId(fileId);
            dto.setOssSp(fsBucket.getOssSp());
            dto.setBucket(bucket);
            dto.setPreviewUrl(previewUrl);
            return dto;
        } catch (OSSException oe) {
            log.error("upload file to ali oss faild: errCode: {}, requestId: {}, hostId: {}, msg: {}",
                oe.getErrorCode(), oe.getRequestId(), oe.getHostId(), oe.getErrorMessage());
            throw new RuntimeException(oe);
        } catch (ClientException ce) {
            log.error("upload file to ali oss faild: {}", ce.getMessage());
            throw new RuntimeException(ce);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            ossClient.shutdown();
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

    private String sign(String fileId, MdmFsBucket fsBucket, Integer expire, TimeUnit timeUnit, OSS ossClient) {
        List<String> signs = sign(Collections.singletonList(fileId), fsBucket, expire, timeUnit, ossClient);
        return signs.get(0);
    }

    private List<String> sign(List<String> fileIds, MdmFsBucket fsBucket, Integer expire, TimeUnit timeUnit, OSS ossClient) {
        List<String> result = new ArrayList<>();
        String bucket = fsBucket.getBucket();
        String endpointOuter = fsBucket.getEndpointOuter();
        boolean needShutdown = false;

        long millis = timeUnit.toMillis(expire);
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MILLISECOND, (int)millis);
        Date expireTime = calendar.getTime();

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
                if (fileId.startsWith(FsConstant.PUBLIC_PREFIX)) {
                    // 为开放访问的 file, 直接加上域名即可
                    result.add(endpointOuter + "/" + fileId);
                    continue;
                }

                if (ossClient == null) {
                    needShutdown = true;
                    ossClient = getOssClient(fsBucket);
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
            throw new RuntimeException(e);
        } finally {
            if (needShutdown && ossClient != null) {
                ossClient.shutdown();
            }
        }
        return result;
    }


    /**
     * OSS 多文件删除
     */
    @Override
    public Integer delete(List<String> fileIds, MdmFsBucket fsBucket) {
        String bucket = fsBucket.getBucket();
        OSS ossClient = getOssClient(fsBucket);

        try {
            // 删除文件
            DeleteObjectsRequest request = new DeleteObjectsRequest(bucket);
            request.setKeys(fileIds);
            DeleteObjectsResult objectsResult = ossClient.deleteObjects(request);
            List<String> objects = objectsResult.getDeletedObjects();
            return objects.size();
        } catch (OSSException oe) {
            log.error("delete file from ali oss faild: errCode: {}, requestId: {}, hostId: {}, msg: {}",
                oe.getErrorCode(), oe.getRequestId(), oe.getHostId(), oe.getErrorMessage());
        } catch (ClientException ce) {
            log.error("delete file from ali oss faild: {}", ce.getMessage());
        } finally {
            ossClient.shutdown();
        }
        return 0;
    }


    private static OSS getOssClient(MdmFsBucket fsBucket) {
        String endpointInner = fsBucket.getEndpointInner();
        String accessKey = fsBucket.getAccessKey();
        String secretKey = fsBucket.getSecretKey();
        if (!endpointInner.startsWith("http")) {
            endpointInner = "https://" + endpointInner;
        }
        DefaultCredentialProvider credentialProvider = getCredentialProvider(accessKey, secretKey);
        return new OSSClientBuilder().build(endpointInner, credentialProvider);
    }

    private static DefaultCredentialProvider getCredentialProvider(String accessKeyId, String accessKeySecret) {
        return CredentialsProviderFactory.newDefaultCredentialProvider(accessKeyId, accessKeySecret);
    }
}