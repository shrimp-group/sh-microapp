package com.wkclz.micro.fileos.api;

import com.wkclz.micro.fileos.bean.dto.FileosUploadRequest;
import com.wkclz.micro.fileos.bean.dto.MdmFileosRecordDto;
import com.wkclz.micro.fileos.bean.dto.MultipartCompleteRequest;
import com.wkclz.micro.fileos.bean.dto.MultipartUploadInitRequest;
import com.wkclz.micro.fileos.bean.dto.MultipartUploadInitResponse;
import org.springframework.web.multipart.MultipartFile;

public interface FileosUploadApi {

    MdmFileosRecordDto upload(MultipartFile file);

    MdmFileosRecordDto upload(MultipartFile file, String category);

    MdmFileosRecordDto upload(MultipartFile file, String category, String bucketName);

    MdmFileosRecordDto upload(MultipartFile file, String category, String bucketName, Boolean isPublic);

    MdmFileosRecordDto upload(MultipartFile file, String category, String bucketName, Boolean isPublic, String fileName);

    MdmFileosRecordDto upload(MultipartFile file, FileosUploadRequest request);

    MultipartUploadInitResponse initMultipartUpload(MultipartUploadInitRequest request);

    MdmFileosRecordDto completeMultipartUpload(MultipartCompleteRequest request);

    void abortMultipartUpload(String uploadId, String fileId, String bucketName, String ossSp);

}
