package com.wkclz.micro.fileos.api;

import com.wkclz.micro.fileos.bean.dto.MdmFileosRecordDto;
import com.wkclz.micro.fileos.bean.dto.MultipartCompleteRequest;
import com.wkclz.micro.fileos.bean.dto.MultipartUploadInitRequest;
import com.wkclz.micro.fileos.bean.dto.MultipartUploadInitResponse;
import com.wkclz.micro.fileos.bean.dto.PresignCompleteRequest;
import com.wkclz.micro.fileos.bean.dto.PresignUploadRequest;
import com.wkclz.micro.fileos.bean.dto.PresignUploadResponse;

import java.util.List;

public interface FileosPresignUploadApi {

    PresignUploadResponse presignUpload(PresignUploadRequest request);

    List<PresignUploadResponse> presignUploadBatch(List<PresignUploadRequest> requests);

    MultipartUploadInitResponse initMultipartUpload(MultipartUploadInitRequest request);

    MdmFileosRecordDto completeMultipartUpload(MultipartCompleteRequest request);

    void abortMultipartUpload(String uploadId, String fileId, String bucketName, String ossSp);

    MdmFileosRecordDto presignComplete(PresignCompleteRequest request);

    List<MdmFileosRecordDto> presignCompleteBatch(List<PresignCompleteRequest> requests);

}
