package com.wkclz.micro.fileos.api;

import com.wkclz.micro.fileos.bean.req.MultipartCompleteReq;
import com.wkclz.micro.fileos.bean.req.MultipartUploadInitReq;
import com.wkclz.micro.fileos.bean.req.PresignCompleteReq;
import com.wkclz.micro.fileos.bean.req.PresignUploadReq;
import com.wkclz.micro.fileos.bean.resp.MultipartUploadInitResp;
import com.wkclz.micro.fileos.bean.resp.PresignUploadResp;
import com.wkclz.micro.fileos.bean.resp.RecordResp;

import java.util.List;

public interface FileosPresignUploadApi {

    PresignUploadResp presignUpload(PresignUploadReq request);

    List<PresignUploadResp> presignUploadBatch(List<PresignUploadReq> requests);

    MultipartUploadInitResp initMultipartUpload(MultipartUploadInitReq request);

    RecordResp completeMultipartUpload(MultipartCompleteReq request);

    void abortMultipartUpload(String uploadId, String fileId, String bucketName, String ossSp);

    RecordResp presignComplete(PresignCompleteReq request);

    List<RecordResp> presignCompleteBatch(List<PresignCompleteReq> requests);

}
