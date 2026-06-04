package com.wkclz.micro.fileos.service;

import com.wkclz.micro.fileos.bean.dto.CompletedPartInfo;
import com.wkclz.micro.fileos.bean.dto.MdmFileosRecordDto;
import com.wkclz.micro.fileos.bean.dto.MultipartUploadInitResponse;
import com.wkclz.micro.fileos.bean.dto.PresignUploadResponse;
import com.wkclz.micro.fileos.bean.entity.MdmFileosBucket;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.List;
import java.util.concurrent.TimeUnit;

public interface FileosService {

    MdmFileosRecordDto upload(MultipartFile file, MdmFileosBucket bucket, String fileId, String category, Boolean isPublic);

    String sign(String file, MdmFileosBucket bucket, Integer expire, TimeUnit timeUnit);

    List<String> sign(List<String> files, MdmFileosBucket bucket, Integer expire, TimeUnit timeUnit);

    Integer delete(List<String> files, MdmFileosBucket bucket);

    PresignUploadResponse presignUpload(String fileId, MdmFileosBucket bucket, String contentType, Integer expireMinutes);

    MultipartUploadInitResponse initMultipartUpload(String fileId, MdmFileosBucket bucket, String contentType, Integer partCount, Integer expireMinutes);

    void completeMultipartUpload(String uploadId, String fileId, MdmFileosBucket bucket, List<CompletedPartInfo> parts);

    void abortMultipartUpload(String uploadId, String fileId, MdmFileosBucket bucket);

    InputStream download(String fileId, MdmFileosBucket bucket);

    InputStream download(String fileId, MdmFileosBucket bucket, long offset, long length);

}
