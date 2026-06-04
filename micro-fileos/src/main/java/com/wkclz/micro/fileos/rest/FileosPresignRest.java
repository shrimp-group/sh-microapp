package com.wkclz.micro.fileos.rest;

import com.wkclz.core.base.R;
import com.wkclz.micro.fileos.api.FileosPresignUploadApi;
import com.wkclz.micro.fileos.bean.dto.MdmFileosRecordDto;
import com.wkclz.micro.fileos.bean.dto.MultipartCompleteRequest;
import com.wkclz.micro.fileos.bean.dto.MultipartUploadInitRequest;
import com.wkclz.micro.fileos.bean.dto.MultipartUploadInitResponse;
import com.wkclz.micro.fileos.bean.dto.PresignCompleteRequest;
import com.wkclz.micro.fileos.bean.dto.PresignUploadRequest;
import com.wkclz.micro.fileos.bean.dto.PresignUploadResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping(Route.PREFIX)
public class FileosPresignRest {

    @Autowired
    private FileosPresignUploadApi fileosPresignUploadApi;

    @PostMapping(Route.PRESIGN_UPLOAD)
    public R<PresignUploadResponse> presignUpload(@RequestBody PresignUploadRequest request) {
        PresignUploadResponse response = fileosPresignUploadApi.presignUpload(request);
        return R.ok(response);
    }

    @PostMapping(Route.PRESIGN_UPLOAD_BATCH)
    public R<List<PresignUploadResponse>> presignUploadBatch(@RequestBody List<PresignUploadRequest> requests) {
        List<PresignUploadResponse> responses = fileosPresignUploadApi.presignUploadBatch(requests);
        return R.ok(responses);
    }

    @PostMapping(Route.PRESIGN_MULTIPART_INIT)
    public R<MultipartUploadInitResponse> multipartInit(@RequestBody MultipartUploadInitRequest request) {
        log.info("预签名分片上传初始化: fileName={}, bucketName={}", request.getFileName(), request.getBucketName());
        MultipartUploadInitResponse response = fileosPresignUploadApi.initMultipartUpload(request);
        return R.ok(response);
    }

    @PostMapping(Route.PRESIGN_MULTIPART_COMPLETE)
    public R<MdmFileosRecordDto> multipartComplete(@RequestBody MultipartCompleteRequest request) {
        log.info("预签名分片上传完成: fileId={}, uploadId={}", request.getFileId(), request.getUploadId());
        MdmFileosRecordDto dto = fileosPresignUploadApi.completeMultipartUpload(request);
        return R.ok(dto);
    }

    @PostMapping(Route.PRESIGN_MULTIPART_ABORT)
    public R<?> multipartAbort(@RequestParam String uploadId,
                               @RequestParam String fileId,
                               @RequestParam(required = false) String bucketName,
                               @RequestParam(required = false) String ossSp) {
        log.info("预签名分片上传中止: fileId={}, uploadId={}", fileId, uploadId);
        fileosPresignUploadApi.abortMultipartUpload(uploadId, fileId, bucketName, ossSp);
        return R.ok();
    }

    @PostMapping(Route.PRESIGN_COMPLETE)
    public R<MdmFileosRecordDto> presignComplete(@RequestBody PresignCompleteRequest request) {
        log.info("预签名上传完成确认: fileId={}, bucketName={}", request.getFileId(), request.getBucketName());
        MdmFileosRecordDto dto = fileosPresignUploadApi.presignComplete(request);
        return R.ok(dto);
    }

    @PostMapping(Route.PRESIGN_COMPLETE_BATCH)
    public R<List<MdmFileosRecordDto>> presignCompleteBatch(@RequestBody List<PresignCompleteRequest> requests) {
        log.info("预签名上传批量完成确认: count={}", requests.size());
        List<MdmFileosRecordDto> dtos = fileosPresignUploadApi.presignCompleteBatch(requests);
        return R.ok(dtos);
    }

}
