package com.wkclz.micro.fileos.api;

import com.wkclz.micro.fileos.bean.req.MultipartCompleteReq;
import com.wkclz.micro.fileos.bean.req.MultipartUploadInitReq;
import com.wkclz.micro.fileos.bean.req.UploadSimpleReq;
import com.wkclz.micro.fileos.bean.resp.MultipartUploadInitResp;
import com.wkclz.micro.fileos.bean.resp.RecordResp;
import org.springframework.web.multipart.MultipartFile;

public interface FileosUploadApi {

    RecordResp upload(MultipartFile file);

    RecordResp upload(MultipartFile file, String category);

    RecordResp upload(MultipartFile file, String category, String bucketName);

    RecordResp upload(MultipartFile file, String category, String bucketName, Boolean isPublic);

    RecordResp upload(MultipartFile file, String category, String bucketName, Boolean isPublic, String fileName);

    RecordResp upload(MultipartFile file, UploadSimpleReq request);

    MultipartUploadInitResp initMultipartUpload(MultipartUploadInitReq request);

    RecordResp completeMultipartUpload(MultipartCompleteReq request);

    void abortMultipartUpload(String uploadId, String fileId, String bucketName, String ossSp);

}
