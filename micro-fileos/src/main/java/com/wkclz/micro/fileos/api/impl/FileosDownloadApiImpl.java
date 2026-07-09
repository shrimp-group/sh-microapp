package com.wkclz.micro.fileos.api.impl;

import com.wkclz.iam.contract.context.PrincipalContext;
import com.wkclz.micro.fileos.api.FileosDownloadApi;
import com.wkclz.micro.fileos.bean.entity.MdmFileosBucket;
import com.wkclz.micro.fileos.bean.entity.MdmFileosRecord;
import com.wkclz.micro.fileos.service.FileosService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.io.InputStream;

@Slf4j
@Service
public class FileosDownloadApiImpl extends AbstractFileosApi implements FileosDownloadApi {

    @Override
    public InputStream download(String fileId) {
        String tmpFileId = getFileId(fileId);
        String tenantCode = PrincipalContext.getTenantCode();
        MdmFileosRecord record = mdmFileosRecordService.getRecordByFileId(tmpFileId, tenantCode);
        if (record == null) {
            log.warn("文件: {} 不存在，无法下载", fileId);
            return null;
        }
        MdmFileosBucket bucket = getBucket(record.getBucketName());
        FileosService service = getApi(bucket);
        log.info("下载文件, fileId={}, bucketName={}", tmpFileId, bucket.getBucketName());
        return service.download(record.getFileId(), bucket);
    }

    @Override
    public InputStream download(String fileId, long offset, long length) {
        String tmpFileId = getFileId(fileId);
        String tenantCode = PrincipalContext.getTenantCode();
        MdmFileosRecord record = mdmFileosRecordService.getRecordByFileId(tmpFileId, tenantCode);
        if (record == null) {
            log.warn("文件: {} 不存在，无法下载", fileId);
            return null;
        }
        MdmFileosBucket bucket = getBucket(record.getBucketName());
        FileosService service = getApi(bucket);
        log.info("范围下载文件, fileId={}, offset={}, length={}, bucketName={}", tmpFileId, offset, length, bucket.getBucketName());
        return service.download(record.getFileId(), bucket, offset, length);
    }

}
