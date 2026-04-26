package com.wkclz.micro.file.api.impl;

import com.wkclz.core.exception.ValidationException;
import com.wkclz.iam.sdk.helper.SessionHelper;
import com.wkclz.micro.file.api.FileDeleteApi;
import com.wkclz.micro.file.bean.entity.MdmFileBucket;
import com.wkclz.micro.file.bean.entity.MdmFileRecord;
import com.wkclz.micro.file.service.FileService;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class FileDeleteApiImpl extends AbstractFileApi implements FileDeleteApi {

    @Override
    public Integer delete(String fileId) {
        return delete(Collections.singletonList(fileId));
    }

    @Override
    public Integer delete(List<String> fileIds) {
        List<MdmFileRecord> fsFiles = mdmFileRecordService.getFilesByFileIds(fileIds);
        if (CollectionUtils.isEmpty(fsFiles)) {
            return 0;
        }
        List<String> tenantCodes = fsFiles.stream().map(MdmFileRecord::getTenantCode).distinct().toList();
        if (tenantCodes.size() > 1) {
            throw ValidationException.of("待删除的文件可能来自多个租户，请选择一个租户进行操作!");
        }
        if (!tenantCodes.getFirst().equals(SessionHelper.getTenantCode())) {
            throw ValidationException.of("待删除的文件可能来自其他租户，请选择其他租户进行操作!");
        }
        if (fsFiles.size() != fileIds.size()) {
            throw ValidationException.of("待删除的文件可能已经丢失，请核实后再操作!");
        }
        Map<String, List<MdmFileRecord>> filesSpMap = fsFiles.stream().collect(Collectors.groupingBy(MdmFileRecord::getOssSp));

        for (Map.Entry<String, List<MdmFileRecord>> entry : filesSpMap.entrySet()) {
            String ossSp = entry.getKey();
            List<MdmFileRecord> spFiles = entry.getValue();

            FileService service = getApi(ossSp);
            Map<String, List<MdmFileRecord>> filesBucketMap = spFiles.stream().collect(Collectors.groupingBy(MdmFileRecord::getBucket));

            for (String bucket : filesBucketMap.keySet()) {
                List<MdmFileRecord> bucketFiles = filesBucketMap.get(bucket);
                MdmFileBucket fsBucket = getBucket(bucket);
                List<String> collect = bucketFiles.stream().map(MdmFileRecord::getFileId).collect(Collectors.toList());
                service.delete(collect, fsBucket);
            }
        }
        List<Long> ids = fsFiles.stream().map(MdmFileRecord::getId).toList();
        MdmFileRecord forDelete = new MdmFileRecord();
        forDelete.setIds(ids);
        mdmFileRecordService.deleteByIds(forDelete);
        return fsFiles.size();
    }

}
