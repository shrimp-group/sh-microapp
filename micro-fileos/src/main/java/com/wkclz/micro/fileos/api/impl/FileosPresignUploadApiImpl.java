package com.wkclz.micro.fileos.api.impl;

import com.wkclz.core.exception.ValidationException;
import com.wkclz.iam.contract.context.PrincipalContext;
import com.wkclz.micro.fileos.api.FileosPresignUploadApi;
import com.wkclz.micro.fileos.bean.entity.MdmFileosBucket;
import com.wkclz.micro.fileos.bean.entity.MdmFileosMultipart;
import com.wkclz.micro.fileos.bean.entity.MdmFileosRecord;
import com.wkclz.micro.fileos.bean.enums.UploadStatusEnum;
import com.wkclz.micro.fileos.bean.enums.UploadTypeEnum;
import com.wkclz.micro.fileos.helper.FileTypeHelper;
import com.wkclz.micro.fileos.bean.req.MultipartCompleteReq;
import com.wkclz.micro.fileos.bean.req.MultipartUploadInitReq;
import com.wkclz.micro.fileos.bean.req.PresignCompleteReq;
import com.wkclz.micro.fileos.bean.req.PresignUploadReq;
import com.wkclz.micro.fileos.bean.resp.MultipartUploadInitResp;
import com.wkclz.micro.fileos.bean.resp.PresignUploadResp;
import com.wkclz.micro.fileos.bean.resp.RecordResp;
import com.wkclz.micro.fileos.service.FileosService;
import com.wkclz.micro.fileos.service.MdmFileosMultipartService;
import com.wkclz.micro.fileos.utils.OssUtil;
import com.wkclz.tool.utils.BeanUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Slf4j
@Service
public class FileosPresignUploadApiImpl extends AbstractFileosApi implements FileosPresignUploadApi {

    @Autowired
    private MdmFileosMultipartService mdmFileosMultipartService;

    @Override
    public PresignUploadResp presignUpload(PresignUploadReq request) {
        validatePresignRequest(request);
        String category = getCategory(request.getCategory());
        MdmFileosBucket bucket = getBucket(request.getBucketName());
        FileosService service = getApi(bucket);
        String tenantCode = PrincipalContext.getTenantCode();

        String fileId = pathHelper.getFullName(category, request.getFileName(), request.getIsPublic());
        String contentType = request.getContentType();
        if (StringUtils.isBlank(contentType)) {
            contentType = OssUtil.getContentType(request.getFileName());
        }
        Integer expireMinutes = request.getExpireMinutes();
        if (expireMinutes == null || expireMinutes <= 0) {
            expireMinutes = fileosConfig.getPresignExpireMinutes();
        }

        log.info("预签名上传, fileId={}, category={}, bucketName={}", fileId, category, bucket.getBucketName());
        PresignUploadResp resp = service.presignUpload(fileId, bucket, contentType, expireMinutes);

        MdmFileosRecord record = new MdmFileosRecord();
        record.setTenantCode(tenantCode);
        record.setFileId(fileId);
        record.setFileName(request.getFileName());
        record.setFileType(FileTypeHelper.getExtName(request.getFileName()));
        record.setFileSize(request.getFileSize());
        record.setContentType(contentType);
        record.setCategory(category);
        record.setDirPath(pathHelper.extractDirPath(fileId));
        record.setIsPublic(request.getIsPublic() != null && request.getIsPublic() ? 1 : 0);
        record.setOssSp(bucket.getOssSp());
        record.setBucketName(bucket.getBucketName());
        record.setUploadType(UploadTypeEnum.PRESIGN.name());
        record.setUploadStatus(UploadStatusEnum.UPLOADING.name());
        record.setImageProcess(request.getImageProcess());
        mdmFileosRecordService.insert(record);

        resp.setFileId(fileId);
        resp.setOssSp(bucket.getOssSp());
        resp.setBucketName(bucket.getBucketName());
        resp.setContentType(contentType);
        resp.setExpireMinutes(expireMinutes);
        return resp;
    }

    @Override
    public List<PresignUploadResp> presignUploadBatch(List<PresignUploadReq> requests) {
        if (CollectionUtils.isEmpty(requests)) {
            throw ValidationException.of("请求列表不能为空");
        }
        List<PresignUploadResp> responses = new ArrayList<>();
        for (PresignUploadReq request : requests) {
            responses.add(presignUpload(request));
        }
        return responses;
    }

    @Override
    public MultipartUploadInitResp initMultipartUpload(MultipartUploadInitReq request) {
        validateMultipartRequest(request);
        String category = getCategory(request.getCategory());
        MdmFileosBucket bucket = getBucket(request.getBucketName());
        FileosService service = getApi(bucket);
        String tenantCode = PrincipalContext.getTenantCode();

        String fileId = pathHelper.getFullName(category, request.getFileName(), request.getIsPublic());
        String contentType = request.getContentType();
        if (StringUtils.isBlank(contentType)) {
            contentType = OssUtil.getContentType(request.getFileName());
        }
        Integer expireMinutes = request.getExpireMinutes();
        if (expireMinutes == null || expireMinutes <= 0) {
            expireMinutes = fileosConfig.getMultipartExpireMinutes();
        }
        Integer partCount = request.getPartCount();
        if (partCount == null || partCount <= 0) {
            throw ValidationException.of("分片数量必须大于0");
        }

        log.info("预签名分片上传初始化, fileId={}, partCount={}, bucketName={}", fileId, partCount, bucket.getBucketName());
        MultipartUploadInitResp resp = service.initMultipartUpload(fileId, bucket, contentType, partCount, expireMinutes);

        MdmFileosMultipart multipart = new MdmFileosMultipart();
        multipart.setTenantCode(tenantCode);
        multipart.setUploadId(resp.getUploadId());
        multipart.setFileId(fileId);
        multipart.setFileName(request.getFileName());
        multipart.setFileSize(request.getFileSize());
        multipart.setContentType(contentType);
        multipart.setCategory(category);
        multipart.setIsPublic(request.getIsPublic() != null && request.getIsPublic() ? 1 : 0);
        multipart.setOssSp(bucket.getOssSp());
        multipart.setBucketName(bucket.getBucketName());
        multipart.setPartCount(partCount);
        multipart.setStatus(UploadStatusEnum.UPLOADING.name());
        long expireMs = (expireMinutes != null ? expireMinutes : 60) * 60_000L;
        multipart.setExpireTime(new Date(System.currentTimeMillis() + expireMs));
        mdmFileosMultipartService.insert(multipart);

        resp.setFileId(fileId);
        resp.setOssSp(bucket.getOssSp());
        resp.setBucketName(bucket.getBucketName());
        return resp;
    }

    @Override
    public RecordResp completeMultipartUpload(MultipartCompleteReq request) {
        if (StringUtils.isBlank(request.getUploadId())) {
            throw ValidationException.of("uploadId 不能为空");
        }
        if (StringUtils.isBlank(request.getFileId())) {
            throw ValidationException.of("fileId 不能为空");
        }
        if (CollectionUtils.isEmpty(request.getParts())) {
            throw ValidationException.of("分片信息不能为空");
        }

        MdmFileosBucket bucket = getBucket(request.getBucketName());
        FileosService service = getApi(bucket);
        String tenantCode = PrincipalContext.getTenantCode();
        String category = getCategory(request.getCategory());

        log.info("完成预签名分片上传, uploadId={}, fileId={}, bucketName={}", request.getUploadId(), request.getFileId(), bucket.getBucketName());
        service.completeMultipartUpload(request.getUploadId(), request.getFileId(), bucket, request.getParts());

        MdmFileosRecord record = new MdmFileosRecord();
        record.setTenantCode(tenantCode);
        record.setFileId(request.getFileId());
        record.setFileName(request.getFileName());
        record.setFileType(FileTypeHelper.getExtName(request.getFileName()));
        record.setFileSize(request.getFileSize());
        record.setContentType(OssUtil.getContentType(request.getFileName()));
        record.setCategory(category);
        record.setDirPath(pathHelper.extractDirPath(request.getFileId()));
        record.setIsPublic(request.getIsPublic() != null && request.getIsPublic() ? 1 : 0);
        record.setOssSp(bucket.getOssSp());
        record.setBucketName(bucket.getBucketName());
        record.setUploadType(UploadTypeEnum.MULTIPART.name());
        record.setUploadId(request.getUploadId());
        record.setUploadStatus(UploadStatusEnum.COMPLETED.name());
        mdmFileosRecordService.insert(record);

        MdmFileosMultipart multipart = new MdmFileosMultipart();
        multipart.setUploadId(request.getUploadId());
        multipart.setStatus(UploadStatusEnum.COMPLETED.name());
        mdmFileosMultipartService.updateMultipartFileStatus(multipart);

        ensureDirectoryAsync(request.getFileId(), bucket.getBucketName(), tenantCode, request.getFileSize() != null ? request.getFileSize() : 0L);
        return BeanUtil.cp(record, RecordResp.class);
    }

    @Override
    public void abortMultipartUpload(String uploadId, String fileId, String bucketName, String ossSp) {
        if (StringUtils.isBlank(uploadId)) {
            throw ValidationException.of("uploadId 不能为空");
        }
        if (StringUtils.isBlank(fileId)) {
            throw ValidationException.of("fileId 不能为空");
        }

        MdmFileosBucket bucket = getBucket(bucketName);
        FileosService service = getApi(bucket);
        log.info("中止预签名分片上传, uploadId={}, fileId={}", uploadId, fileId);
        service.abortMultipartUpload(uploadId, fileId, bucket);

        MdmFileosMultipart multipart = new MdmFileosMultipart();
        multipart.setUploadId(uploadId);
        multipart.setStatus(UploadStatusEnum.ABORTED.name());
        mdmFileosMultipartService.updateMultipartFileStatus(multipart);
    }

    @Override
    public RecordResp presignComplete(PresignCompleteReq request) {
        if (StringUtils.isBlank(request.getFileId())) {
            throw ValidationException.of("fileId 不能为空");
        }
        if (StringUtils.isBlank(request.getFileName())) {
            throw ValidationException.of("fileName 不能为空");
        }

        String tenantCode = PrincipalContext.getTenantCode();
        MdmFileosRecord existing = mdmFileosRecordService.getRecordByFileId(request.getFileId(), tenantCode);
        if (existing == null) {
            throw ValidationException.of("预签名上传记录不存在, fileId={}", request.getFileId());
        }

        existing.setUploadStatus(UploadStatusEnum.COMPLETED.name());
        if (request.getFileSize() != null) {
            existing.setFileSize(request.getFileSize());
        }
        if (StringUtils.isNotBlank(request.getImageProcess())) {
            existing.setImageProcess(request.getImageProcess());
        }
        mdmFileosRecordService.updateByIdSelective(existing);

        log.info("预签名上传完成, fileId={}, bucketName={}", request.getFileId(), existing.getBucketName());
        ensureDirectoryAsync(existing.getFileId(), existing.getBucketName(), tenantCode, existing.getFileSize() != null ? existing.getFileSize() : 0L);
        return BeanUtil.cp(existing, RecordResp.class);
    }

    @Override
    public List<RecordResp> presignCompleteBatch(List<PresignCompleteReq> requests) {
        if (CollectionUtils.isEmpty(requests)) {
            throw ValidationException.of("请求列表不能为空");
        }
        List<RecordResp> responses = new ArrayList<>();
        for (PresignCompleteReq request : requests) {
            responses.add(presignComplete(request));
        }
        return responses;
    }

    private void validatePresignRequest(PresignUploadReq request) {
        if (request == null) {
            throw ValidationException.of("请求不能为空");
        }
        if (StringUtils.isBlank(request.getFileName())) {
            throw ValidationException.of("文件名不能为空");
        }
        if (request.getFileSize() == null || request.getFileSize() <= 0) {
            throw ValidationException.of("文件大小必须大于0");
        }

        long size = request.getFileSize();
        if (fileTypeHelper.isImage(request.getFileName())) {
            Integer imageMaxSizeMb = fileosConfig.getImageMaxSizeMb();
            if (size > imageMaxSizeMb * 1024L * 1024) {
                throw ValidationException.of("上传图片不能超过 {}Mb", imageMaxSizeMb);
            }
        } else if (fileTypeHelper.isVideo(request.getFileName())) {
            Integer videoMaxSizeMb = fileosConfig.getVideoMaxSizeMb();
            if (size > videoMaxSizeMb * 1024L * 1024) {
                throw ValidationException.of("上传视频不能超过 {}Mb", videoMaxSizeMb);
            }
        } else {
            Integer maxSizeMb = fileosConfig.getMaxSizeMb();
            if (size > maxSizeMb * 1024L * 1024) {
                throw ValidationException.of("上传文件不能超过 {}Mb", maxSizeMb);
            }
        }
    }

    private void validateMultipartRequest(MultipartUploadInitReq request) {
        if (request == null) {
            throw ValidationException.of("请求不能为空");
        }
        if (StringUtils.isBlank(request.getFileName())) {
            throw ValidationException.of("文件名不能为空");
        }
        if (request.getFileSize() == null || request.getFileSize() <= 0) {
            throw ValidationException.of("文件大小必须大于0");
        }
    }

}
