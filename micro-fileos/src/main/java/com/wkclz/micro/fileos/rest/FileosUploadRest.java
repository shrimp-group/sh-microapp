package com.wkclz.micro.fileos.rest;

import com.wkclz.core.base.R;
import com.wkclz.micro.fileos.api.FileosUploadApi;
import com.wkclz.micro.fileos.bean.dto.MdmFileosRecordDto;
import com.wkclz.micro.fileos.bean.dto.MultipartCompleteRequest;
import com.wkclz.micro.fileos.bean.dto.MultipartUploadInitRequest;
import com.wkclz.micro.fileos.bean.dto.MultipartUploadInitResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@RestController
@RequestMapping(Route.PREFIX)
public class FileosUploadRest {

    @Autowired
    private FileosUploadApi fileosUploadApi;

    @PostMapping(Route.UPLOAD_SIMPLE)
    public R<MdmFileosRecordDto> uploadSimple(@RequestParam MultipartFile file,
                                              @RequestParam(required = false) String category,
                                              @RequestParam(required = false) String bucketName,
                                              @RequestParam(required = false) String fileName) {
        log.info("简单上传: fileName={}, category={}, bucketName={}", fileName != null ? fileName : file.getOriginalFilename(), category, bucketName);
        MdmFileosRecordDto dto = fileosUploadApi.upload(file, category, bucketName, null, fileName);
        return R.ok(dto);
    }

    @PostMapping(Route.UPLOAD_SIMPLE_PUBLIC)
    public R<MdmFileosRecordDto> uploadSimplePublic(@RequestParam MultipartFile file,
                                                     @RequestParam(required = false) String category,
                                                     @RequestParam(required = false) String bucketName,
                                                     @RequestParam(required = false) String fileName) {
        log.info("公开上传: fileName={}, category={}, bucketName={}", fileName != null ? fileName : file.getOriginalFilename(), category, bucketName);
        MdmFileosRecordDto dto = fileosUploadApi.upload(file, category, bucketName, true, fileName);
        return R.ok(dto);
    }

    @PostMapping(Route.UPLOAD_MULTIPART_INIT)
    public R<MultipartUploadInitResponse> multipartInit(@RequestBody MultipartUploadInitRequest request) {
        log.info("分片上传初始化: fileName={}, bucketName={}", request.getFileName(), request.getBucketName());
        MultipartUploadInitResponse response = fileosUploadApi.initMultipartUpload(request);
        return R.ok(response);
    }

    @PostMapping(Route.UPLOAD_MULTIPART_COMPLETE)
    public R<MdmFileosRecordDto> multipartComplete(@RequestBody MultipartCompleteRequest request) {
        log.info("分片上传完成: fileId={}, uploadId={}", request.getFileId(), request.getUploadId());
        MdmFileosRecordDto dto = fileosUploadApi.completeMultipartUpload(request);
        return R.ok(dto);
    }

    @PostMapping(Route.UPLOAD_MULTIPART_ABORT)
    public R<?> multipartAbort(@RequestParam String uploadId,
                               @RequestParam String fileId,
                               @RequestParam(required = false) String bucketName,
                               @RequestParam(required = false) String ossSp) {
        log.info("分片上传中止: fileId={}, uploadId={}", fileId, uploadId);
        fileosUploadApi.abortMultipartUpload(uploadId, fileId, bucketName, ossSp);
        return R.ok();
    }

}
