package com.wkclz.micro.fileos.api.impl;

import com.wkclz.core.exception.ValidationException;
import com.wkclz.core.user.UserContext;
import com.wkclz.micro.fileos.api.FileosDeleteApi;
import com.wkclz.micro.fileos.bean.entity.MdmFileosBucket;
import com.wkclz.micro.fileos.bean.entity.MdmFileosRecord;
import com.wkclz.micro.fileos.service.FileosService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
public class FileosDeleteApiImpl extends AbstractFileosApi implements FileosDeleteApi {

    @Override
    public Integer delete(String fileId) {
        MdmFileosRecord record = mdmFileosRecordService.getRecordByFileId(fileId, null);
        if (record == null) {
            return 0;
        }
        String currentTenantCode = UserContext.getTenantCode();
        if (!currentTenantCode.equals(record.getTenantCode())) {
            log.warn("删除文件租户校验失败, 当前租户: {}, 文件所属租户: {}, fileId: {}", currentTenantCode, record.getTenantCode(), fileId);
            throw ValidationException.of("无权删除其他租户的文件");
        }
        return delete(Collections.singletonList(fileId));
    }

    @Override
    public Integer delete(List<String> fileIds) {
        List<MdmFileosRecord> records = mdmFileosRecordService.getRecordByFileIds(fileIds, null);
        if (CollectionUtils.isEmpty(records)) {
            return 0;
        }

        String currentTenantCode = UserContext.getTenantCode();
        List<MdmFileosRecord> otherTenantRecords = records.stream()
            .filter(r -> !currentTenantCode.equals(r.getTenantCode()))
            .toList();
        if (!otherTenantRecords.isEmpty()) {
            log.warn("批量删除文件租户校验失败, 当前租户: {}, 包含其他租户文件数: {}", currentTenantCode, otherTenantRecords.size());
            throw ValidationException.of("无权删除其他租户的文件");
        }

        if (records.size() != fileIds.size()) {
            log.warn("部分文件不存在, 请求删除{}, 实际找到{}", fileIds.size(), records.size());
        }

        Map<String, List<MdmFileosRecord>> ossSpMap = records.stream()
            .collect(Collectors.groupingBy(MdmFileosRecord::getOssSp));

        for (Map.Entry<String, List<MdmFileosRecord>> entry : ossSpMap.entrySet()) {
            String ossSp = entry.getKey();
            List<MdmFileosRecord> spFiles = entry.getValue();

            FileosService service = getApi(ossSp);
            Map<String, List<MdmFileosRecord>> bucketMap = spFiles.stream()
                .collect(Collectors.groupingBy(MdmFileosRecord::getBucketName));

            for (Map.Entry<String, List<MdmFileosRecord>> bucketEntry : bucketMap.entrySet()) {
                String bucketName = bucketEntry.getKey();
                List<MdmFileosRecord> bucketFiles = bucketEntry.getValue();
                MdmFileosBucket bucket = getBucket(bucketName);
                List<String> ids = bucketFiles.stream().map(MdmFileosRecord::getFileId).collect(Collectors.toList());
                log.info("删除文件, ossSp={}, bucketName={}, count={}", ossSp, bucketName, ids.size());
                service.delete(ids, bucket);
            }
        }

        List<Long> ids = records.stream().map(MdmFileosRecord::getId).toList();
        MdmFileosRecord forDelete = new MdmFileosRecord();
        forDelete.setIds(ids);
        mdmFileosRecordService.deleteByIds(forDelete);

        for (MdmFileosRecord record : records) {
            if (record.getDirPath() != null && record.getFileSize() != null) {
                directoryHelper.updateDirectoryStatsOnDelete(
                    record.getDirPath(), record.getBucketName(), record.getTenantCode(), record.getFileSize());
            }
        }

        return records.size();
    }

}
