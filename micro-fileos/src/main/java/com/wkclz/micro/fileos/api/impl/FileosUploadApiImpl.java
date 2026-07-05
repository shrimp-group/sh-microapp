package com.wkclz.micro.fileos.api.impl;

import com.wkclz.core.exception.ValidationException;
import com.wkclz.core.user.UserContext;
import com.wkclz.micro.fileos.api.FileosSignApi;
import com.wkclz.micro.fileos.api.FileosUploadApi;
import com.wkclz.micro.fileos.bean.entity.MdmFileosBucket;
import com.wkclz.micro.fileos.bean.entity.MdmFileosMultipart;
import com.wkclz.micro.fileos.bean.entity.MdmFileosRecord;
import com.wkclz.micro.fileos.bean.enums.UploadStatusEnum;
import com.wkclz.micro.fileos.bean.enums.UploadTypeEnum;
import com.wkclz.micro.fileos.helper.FileTypeHelper;
import com.wkclz.micro.fileos.bean.req.MultipartCompleteReq;
import com.wkclz.micro.fileos.bean.req.MultipartUploadInitReq;
import com.wkclz.micro.fileos.bean.req.UploadSimpleReq;
import com.wkclz.micro.fileos.bean.resp.MultipartUploadInitResp;
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
import org.springframework.web.multipart.MultipartFile;

import java.util.Date;

@Slf4j
@Service
public class FileosUploadApiImpl extends AbstractFileosApi implements FileosUploadApi {

    @Autowired
    protected FileosSignApi fileosSignApi;
    @Autowired
    private MdmFileosMultipartService mdmFileosMultipartService;

    @Override
    public RecordResp upload(MultipartFile file) {
        return upload(file, null, null, null);
    }

    @Override
    public RecordResp upload(MultipartFile file, String category) {
        return upload(file, category, null, null);
    }

    @Override
    public RecordResp upload(MultipartFile file, String category, String bucketName) {
        return upload(file, category, bucketName, null);
    }

    @Override
    public RecordResp upload(MultipartFile file, String category, String bucketName, Boolean isPublic) {
        return upload(file, category, bucketName, isPublic, null);
    }

    @Override
    public RecordResp upload(MultipartFile file, String category, String bucketName, Boolean isPublic, String fileName) {
        UploadSimpleReq request = new UploadSimpleReq();
        request.setCategory(category);
        request.setBucketName(bucketName);
        request.setIsPublic(isPublic);
        request.setFileName(fileName);
        return upload(file, request);
    }

    @Override
    public RecordResp upload(MultipartFile file, UploadSimpleReq request) {
        validateFile(file);

        String category = getCategory(request != null ? request.getCategory() : null);
        String bucketName = request != null ? request.getBucketName() : null;
        Boolean isPublic = request != null ? request.getIsPublic() : null;
        String imageProcess = request != null ? request.getImageProcess() : null;
        String requestFileName = request != null ? request.getFileName() : null;

        MdmFileosBucket bucket = getBucket(bucketName);
        String tenantCode = UserContext.getTenantCode();

        String effectiveFileName = StringUtils.isNotBlank(requestFileName) ? requestFileName : file.getOriginalFilename();

        String fileHash = fileHashHelper.computeHash(file);;
        if (Boolean.TRUE.equals(fileosConfig.getHashEnabled())) {
            MdmFileosRecord existing = mdmFileosRecordService.getRecordByFileHash(fileHash, tenantCode);
            if (existing != null) {
                log.info("文件Hash去重命中, fileHash={}, 已有fileId={}, 直接返回已有记录", fileHash, existing.getFileId());
                RecordResp resp = BeanUtil.cp(existing, RecordResp.class);
                resp.setPreviewUrl(fileosSignApi.sign(existing.getFileId()));
                return resp;
            }
        }

        String fileId = pathHelper.getFullName(category, effectiveFileName, isPublic);
        log.info("上传文件, fileId={}, category={}, bucketName={}", fileId, category, bucket.getBucketName());

        FileosService service = getApi(bucket);
        service.upload(file, bucket, fileId, category, isPublic);

        MdmFileosRecord record = new MdmFileosRecord();
        try {
            record.setTenantCode(tenantCode);
            record.setFileId(fileId);
            record.setFileName(effectiveFileName);
            record.setFileType(FileTypeHelper.getExtName(effectiveFileName));
            record.setFileSize(file.getSize());
            record.setFileHash(fileHash);
            record.setContentType(OssUtil.getContentType(effectiveFileName));
            record.setCategory(category);
            record.setDirPath(pathHelper.extractDirPath(fileId));
            record.setIsPublic(isPublic != null && isPublic ? 1 : 0);
            record.setOssSp(bucket.getOssSp());
            record.setBucketName(bucket.getBucketName());
            record.setUploadType(UploadTypeEnum.SIMPLE.name());
            record.setUploadStatus(UploadStatusEnum.COMPLETED.name());
            record.setImageProcess(imageProcess);
            mdmFileosRecordService.insert(record);
        } catch (Exception e) {
            log.error("文件已上传到存储但数据库记录插入失败，请手动清理孤立文件! fileId={}, bucketName={}", fileId, bucket.getBucketName(), e);
            throw e;
        }
        RecordResp resp = BeanUtil.cp(record, RecordResp.class);
        resp.setPreviewUrl(fileosSignApi.sign(record.getFileId()));

        ensureDirectoryAsync(fileId, record.getBucketName(), tenantCode, record.getFileSize());
        return resp;
    }

    @Override
    public MultipartUploadInitResp initMultipartUpload(MultipartUploadInitReq request) {
        if (request == null) {
            throw ValidationException.of("请求不能为空");
        }
        if (StringUtils.isBlank(request.getFileName())) {
            throw ValidationException.of("文件名不能为空");
        }
        if (request.getFileSize() == null || request.getFileSize() <= 0) {
            throw ValidationException.of("文件大小必须大于0");
        }
        if (request.getPartCount() == null || request.getPartCount() <= 0) {
            throw ValidationException.of("分片数量必须大于0");
        }

        String category = getCategory(request.getCategory());
        MdmFileosBucket bucket = getBucket(request.getBucketName());
        FileosService service = getApi(bucket);
        String tenantCode = UserContext.getTenantCode();

        String fileId = pathHelper.getFullName(category, request.getFileName(), request.getIsPublic());
        String contentType = request.getContentType();
        if (StringUtils.isBlank(contentType)) {
            contentType = OssUtil.getContentType(request.getFileName());
        }
        Integer expireMinutes = request.getExpireMinutes();
        if (expireMinutes == null || expireMinutes <= 0) {
            expireMinutes = fileosConfig.getMultipartExpireMinutes();
        }

        log.info("初始化分片上传, fileId={}, partCount={}, bucketName={}", fileId, request.getPartCount(), bucket.getBucketName());
        MultipartUploadInitResp resp = service.initMultipartUpload(fileId, bucket, contentType, request.getPartCount(), expireMinutes);

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
        multipart.setPartCount(request.getPartCount());
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
        String tenantCode = UserContext.getTenantCode();
        String category = getCategory(request.getCategory());

        log.info("完成分片上传, uploadId={}, fileId={}, bucketName={}", request.getUploadId(), request.getFileId(), bucket.getBucketName());
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
        log.info("中止分片上传, uploadId={}, fileId={}", uploadId, fileId);
        service.abortMultipartUpload(uploadId, fileId, bucket);

        MdmFileosMultipart multipart = new MdmFileosMultipart();
        multipart.setUploadId(uploadId);
        multipart.setStatus(UploadStatusEnum.ABORTED.name());
        mdmFileosMultipartService.updateMultipartFileStatus(multipart);
    }

}
