package com.wkclz.micro.fileos.rest;

import com.wkclz.core.base.R;
import com.wkclz.micro.fileos.api.FileosUploadApi;
import com.wkclz.micro.fileos.bean.req.MultipartAbortReq;
import com.wkclz.micro.fileos.bean.req.MultipartCompleteReq;
import com.wkclz.micro.fileos.bean.req.MultipartUploadInitReq;
import com.wkclz.micro.fileos.bean.req.UploadSimpleReq;
import com.wkclz.micro.fileos.bean.resp.MultipartUploadInitResp;
import com.wkclz.micro.fileos.bean.resp.RecordResp;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@RestController
@RequestMapping(Route.PREFIX)
@Tag(name = "文件上传")
@Validated
public class FileosUploadRest {

    @Autowired
    private FileosUploadApi fileosUploadApi;

    @PostMapping(Route.UPLOAD_SIMPLE)
    @Operation(summary = "简单上传")
    public R<RecordResp> uploadSimple(@RequestParam MultipartFile file, @Valid UploadSimpleReq req) {
        log.info("简单上传: req={},", req);
        RecordResp resp = fileosUploadApi.upload(file, req);
        return R.ok(resp);
    }

    @PostMapping(Route.UPLOAD_SIMPLE_PUBLIC)
    @Operation(summary = "公开上传")
    public R<RecordResp> uploadSimplePublic(@RequestParam MultipartFile file, @Valid UploadSimpleReq req) {
        log.info("公开上传: res={}, ", req);
        req.setIsPublic(true);
        RecordResp resp = fileosUploadApi.upload(file, req);
        return R.ok(resp);
    }

    @PostMapping(Route.UPLOAD_MULTIPART_INIT)
    @Operation(summary = "分片上传初始化")
    public R<MultipartUploadInitResp> multipartInit(@Valid @RequestBody MultipartUploadInitReq req) {
        log.info("分片上传初始化: req={}", req);
        MultipartUploadInitResp resp = fileosUploadApi.initMultipartUpload(req);
        return R.ok(resp);
    }

    @PostMapping(Route.UPLOAD_MULTIPART_COMPLETE)
    @Operation(summary = "分片上传完成")
    public R<RecordResp> multipartComplete(@Valid @RequestBody MultipartCompleteReq req) {
        log.info("分片上传完成: req={}", req);
        RecordResp resp = fileosUploadApi.completeMultipartUpload(req);
        return R.ok(resp);
    }

    @PostMapping(Route.UPLOAD_MULTIPART_ABORT)
    @Operation(summary = "分片上传中止")
    public R<Void> multipartAbort(@Valid MultipartAbortReq req) {
        log.info("分片上传中止: req={}", req);
        fileosUploadApi.abortMultipartUpload(req.getUploadId(), req.getFileId(), req.getBucketName(), req.getOssSp());
        return R.ok();
    }
}
