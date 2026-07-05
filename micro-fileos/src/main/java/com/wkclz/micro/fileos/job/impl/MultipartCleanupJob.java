package com.wkclz.micro.fileos.job.impl;

import com.wkclz.micro.fileos.bean.entity.MdmFileosBucket;
import com.wkclz.micro.fileos.bean.entity.MdmFileosMultipart;
import com.wkclz.micro.fileos.bean.entity.MdmFileosRecord;
import com.wkclz.micro.fileos.bean.enums.OssSpEnum;
import com.wkclz.micro.fileos.config.FileosConfig;
import com.wkclz.micro.fileos.helper.BucketCache;
import com.wkclz.micro.fileos.service.FileosService;
import com.wkclz.micro.fileos.service.MdmFileosMultipartService;
import com.wkclz.micro.fileos.service.MdmFileosRecordService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Component
public class MultipartCleanupJob {

    @Autowired
    private MdmFileosMultipartService mdmFileosMultipartService;
    @Autowired
    private Map<String, FileosService> fileServiceMap;
    @Autowired
    private BucketCache bucketCache;
    @Autowired
    private FileosConfig fileosConfig;
    @Autowired
    private MdmFileosRecordService mdmFileosRecordService;

    public void cleanup() {
        Integer maxAgeHours = fileosConfig.getMultipartMaxAgeHours();
        if (maxAgeHours == null) {
            maxAgeHours = 24;
        }
        LocalDateTime expireLdt = LocalDateTime.now().minusHours(maxAgeHours);
        Date expireTime = Date.from(expireLdt.atZone(ZoneId.systemDefault()).toInstant());

        List<MdmFileosMultipart> expiredList = mdmFileosMultipartService.getExpiredMultipartList();
        if (CollectionUtils.isEmpty(expiredList)) {
            log.info("无过期分片上传记录需要清理");
            return;
        }

        log.info("开始清理过期分片上传记录, 共 {} 条, 过期时间阈值={}", expiredList.size(), expireTime);

        Map<String, List<MdmFileosMultipart>> bucketGrouped = expiredList.stream()
                .collect(Collectors.groupingBy(MdmFileosMultipart::getBucketName));

        int abortedCount = 0;
        int failedCount = 0;

        for (Map.Entry<String, List<MdmFileosMultipart>> entry : bucketGrouped.entrySet()) {
            String bucketName = entry.getKey();
            List<MdmFileosMultipart> multipartList = entry.getValue();
            MdmFileosBucket bucket = bucketCache.get(bucketName);

            FileosService fileosService = null;
            if (bucket != null && StringUtils.isNotBlank(bucket.getOssSp())) {
                try {
                    OssSpEnum ossSpEnum = OssSpEnum.valueOf(bucket.getOssSp());
                    fileosService = fileServiceMap.get(ossSpEnum.getServiceName());
                } catch (IllegalArgumentException e) {
                    log.warn("未知的OSS服务商: ossSp={}, bucketName={}", bucket.getOssSp(), bucketName);
                }
            }

            for (MdmFileosMultipart multipart : multipartList) {
                try {
                    if (fileosService != null && bucket != null) {
                        fileosService.abortMultipartUpload(multipart.getUploadId(), multipart.getFileId(), bucket);
                    }
                    multipart.setStatus("ABORTED");
                    mdmFileosMultipartService.updateByIdSelective(multipart);

                    MdmFileosRecord recordQuery = new MdmFileosRecord();
                    recordQuery.setUploadId(multipart.getUploadId());
                    recordQuery.setUploadStatus("UPLOADING");
                    List<MdmFileosRecord> uploadingRecords = mdmFileosRecordService.selectByEntity(recordQuery);
                    for (MdmFileosRecord record : uploadingRecords) {
                        record.setUploadStatus("ABORTED");
                        mdmFileosRecordService.updateByIdSelective(record);
                    }

                    abortedCount++;
                } catch (Exception e) {
                    failedCount++;
                    log.error("清理分片上传记录失败: uploadId={}, fileId={}, bucketName={}", multipart.getUploadId(), multipart.getFileId(), bucketName, e);
                }
            }
        }

        log.info("分片上传清理完成: 成功 {} 条, 失败 {} 条", abortedCount, failedCount);
    }

}
