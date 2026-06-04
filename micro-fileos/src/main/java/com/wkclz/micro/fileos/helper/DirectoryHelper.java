package com.wkclz.micro.fileos.helper;

import com.wkclz.micro.fileos.bean.entity.MdmFileosDirectory;
import com.wkclz.micro.fileos.service.MdmFileosDirectoryService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
public class DirectoryHelper {

    @Autowired
    private MdmFileosDirectoryService mdmFileosDirectoryService;
    @Autowired
    private PathHelper pathHelper;

    @Async
    public void ensureDirectoryAsync(String fileId, String bucketName, String tenantCode, long fileSize) {
        if (StringUtils.isBlank(fileId)) {
            return;
        }

        String dirPath = pathHelper.extractDirPath(fileId);
        if (StringUtils.isBlank(dirPath)) {
            return;
        }

        List<MdmFileosDirectory> levels = pathHelper.extractDirectoryLevels(fileId, bucketName);
        for (MdmFileosDirectory dir : levels) {
            dir.setTenantCode(tenantCode);
            MdmFileosDirectory existing = mdmFileosDirectoryService.getDirectoryByPath(
                    dir.getDirPath(), dir.getBucketName(), dir.getTenantCode());
            if (existing == null) {
                dir.setFileCount(0L);
                dir.setTotalSize(0L);
                mdmFileosDirectoryService.insert(dir);
                log.info("micro-fileos: 创建目录 dirPath={}, bucketName={}", dir.getDirPath(), dir.getBucketName());
            }
        }

        MdmFileosDirectory targetDir = mdmFileosDirectoryService.getDirectoryByPath(dirPath, bucketName, tenantCode);
        if (targetDir != null) {
            targetDir.setFileCount(targetDir.getFileCount() != null ? targetDir.getFileCount() + 1 : 1L);
            targetDir.setTotalSize(targetDir.getTotalSize() != null ? targetDir.getTotalSize() + fileSize : fileSize);
            mdmFileosDirectoryService.updateByIdSelective(targetDir);
            log.info("micro-fileos: 更新目录统计 dirPath={}, fileCount={}, totalSize={}",
                    dirPath, targetDir.getFileCount(), targetDir.getTotalSize());
        }
    }

    @Async
    public void updateDirectoryStatsOnDelete(String dirPath, String bucketName, String tenantCode, long fileSize) {
        if (StringUtils.isBlank(dirPath)) {
            return;
        }

        MdmFileosDirectory dir = mdmFileosDirectoryService.getDirectoryByPath(dirPath, bucketName, tenantCode);
        if (dir == null) {
            log.warn("micro-fileos: 删除时未找到目录 dirPath={}, bucketName={}", dirPath, bucketName);
            return;
        }

        dir.setFileCount(dir.getFileCount() != null && dir.getFileCount() > 0 ? dir.getFileCount() - 1 : 0L);
        dir.setTotalSize(dir.getTotalSize() != null && dir.getTotalSize() >= fileSize ? dir.getTotalSize() - fileSize : 0L);
        mdmFileosDirectoryService.updateByIdSelective(dir);
        log.info("micro-fileos: 删除后更新目录统计 dirPath={}, fileCount={}, totalSize={}",
                dirPath, dir.getFileCount(), dir.getTotalSize());
    }

}
