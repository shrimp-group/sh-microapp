package com.wkclz.micro.fileos.rest;

import com.wkclz.core.base.R;
import com.wkclz.micro.fileos.api.FileosPresignUploadApi;
import com.wkclz.micro.fileos.bean.req.MultipartAbortReq;
import com.wkclz.micro.fileos.bean.req.MultipartCompleteReq;
import com.wkclz.micro.fileos.bean.req.MultipartUploadInitReq;
import com.wkclz.micro.fileos.bean.req.PresignCompleteReq;
import com.wkclz.micro.fileos.bean.req.PresignUploadReq;
import com.wkclz.micro.fileos.bean.resp.MultipartUploadInitResp;
import com.wkclz.micro.fileos.bean.resp.PresignUploadResp;
import com.wkclz.micro.fileos.bean.resp.RecordResp;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping(Route.PREFIX)
@Tag(name = "预签名上传")
@Validated
public class FileosPresignRest {

    @Autowired
    private FileosPresignUploadApi fileosPresignUploadApi;

    @PostMapping(Route.PRESIGN_UPLOAD)
    @Operation(summary = "预签名简单上传")
    public R<PresignUploadResp> presignUpload(@Valid @RequestBody PresignUploadReq req) {
        PresignUploadResp resp = fileosPresignUploadApi.presignUpload(req);
        return R.ok(resp);
    }

    @PostMapping(Route.PRESIGN_UPLOAD_BATCH)
    @Operation(summary = "批量预签名简单上传")
    public R<List<PresignUploadResp>> presignUploadBatch(@Valid @RequestBody List<PresignUploadReq> reqs) {
        List<PresignUploadResp> respList = fileosPresignUploadApi.presignUploadBatch(reqs);
        return R.ok(respList);
    }

    @PostMapping(Route.PRESIGN_MULTIPART_INIT)
    @Operation(summary = "预签名分片上传初始化")
    public R<MultipartUploadInitResp> multipartInit(@Valid @RequestBody MultipartUploadInitReq req) {
        log.info("预签名分片上传初始化: fileName={}, bucketName={}", req.getFileName(), req.getBucketName());
        MultipartUploadInitResp resp = fileosPresignUploadApi.initMultipartUpload(req);
        return R.ok(resp);
    }

    @PostMapping(Route.PRESIGN_MULTIPART_COMPLETE)
    @Operation(summary = "预签名分片上传完成")
    public R<RecordResp> multipartComplete(@Valid @RequestBody MultipartCompleteReq req) {
        log.info("预签名分片上传完成: fileId={}, uploadId={}", req.getFileId(), req.getUploadId());
        RecordResp resp = fileosPresignUploadApi.completeMultipartUpload(req);
        return R.ok(resp);
    }

    @PostMapping(Route.PRESIGN_MULTIPART_ABORT)
    @Operation(summary = "预签名分片上传中止")
    public R<Void> multipartAbort(@Valid MultipartAbortReq req) {
        log.info("预签名分片上传中止: fileId={}, uploadId={}", req.getFileId(), req.getUploadId());
        fileosPresignUploadApi.abortMultipartUpload(req.getUploadId(), req.getFileId(), req.getBucketName(), req.getOssSp());
        return R.ok();
    }

    @PostMapping(Route.PRESIGN_COMPLETE)
    @Operation(summary = "预签名上传完成确认")
    public R<RecordResp> presignComplete(@Valid @RequestBody PresignCompleteReq req) {
        log.info("预签名上传完成确认: fileId={}, bucketName={}", req.getFileId(), req.getBucketName());
        RecordResp resp = fileosPresignUploadApi.presignComplete(req);
        return R.ok(resp);
    }

    @PostMapping(Route.PRESIGN_COMPLETE_BATCH)
    @Operation(summary = "批量预签名上传完成确认")
    public R<List<RecordResp>> presignCompleteBatch(@Valid @RequestBody List<PresignCompleteReq> reqs) {
        log.info("预签名上传批量完成确认: count={}", reqs.size());
        List<RecordResp> respList = fileosPresignUploadApi.presignCompleteBatch(reqs);
        return R.ok(respList);
    }
}
