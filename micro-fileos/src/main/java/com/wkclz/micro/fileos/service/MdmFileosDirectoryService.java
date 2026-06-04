package com.wkclz.micro.fileos.service;

import com.wkclz.micro.fileos.bean.entity.MdmFileosDirectory;
import com.wkclz.micro.fileos.mapper.MdmFileosDirectoryMapper;
import com.wkclz.mybatis.service.BaseService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class MdmFileosDirectoryService extends BaseService<MdmFileosDirectory, MdmFileosDirectoryMapper> {

    @Autowired
    private MdmFileosDirectoryMapper mapper;

    public List<MdmFileosDirectory> getDirectoryList(String parentPath, String bucketName, String tenantCode) {
        return mapper.getDirectoryList(parentPath, bucketName, tenantCode);
    }

    public List<MdmFileosDirectory> getDirectoryTree(String bucketName, String tenantCode) {
        return mapper.getDirectoryTree(bucketName, tenantCode);
    }

    public MdmFileosDirectory getDirectoryByPath(String dirPath, String bucketName, String tenantCode) {
        if (StringUtils.isBlank(dirPath)) {
            return null;
        }
        return mapper.getDirectoryByPath(dirPath, bucketName, tenantCode);
    }

    @Async
    public void ensureDirectoryAsync(String dirPath, String bucketName, String tenantCode) {
        if (StringUtils.isBlank(dirPath)) {
            return;
        }
        ensureDirectorySync(dirPath, bucketName, tenantCode);
    }

    private void ensureDirectorySync(String dirPath, String bucketName, String tenantCode) {
        MdmFileosDirectory existing = mapper.getDirectoryByPath(dirPath, bucketName, tenantCode);
        if (existing != null) {
            return;
        }
        log.info("Creating directory record: dirPath={}, bucketName={}, tenantCode={}", dirPath, bucketName, tenantCode);

        String parentPath = extractParentPath(dirPath);
        if (StringUtils.isNotBlank(parentPath)) {
            ensureDirectorySync(parentPath, bucketName, tenantCode);
        }

        MdmFileosDirectory directory = new MdmFileosDirectory();
        directory.setTenantCode(tenantCode);
        directory.setBucketName(bucketName);
        directory.setDirPath(dirPath);
        directory.setDirName(extractDirName(dirPath));
        directory.setParentPath(parentPath);
        directory.setDirLevel(calculateDirLevel(dirPath));
        directory.setFileCount(0L);
        directory.setTotalSize(0L);
        insert(directory);
    }

    public void updateDirectoryStats(String dirPath, String bucketName, String tenantCode, long fileSize, boolean isAdd) {
        MdmFileosDirectory directory = mapper.getDirectoryByPath(dirPath, bucketName, tenantCode);
        if (directory == null) {
            log.warn("Directory not found for stats update: dirPath={}, bucketName={}", dirPath, bucketName);
            return;
        }
        if (isAdd) {
            directory.setFileCount(directory.getFileCount() != null ? directory.getFileCount() + 1 : 1);
            directory.setTotalSize(directory.getTotalSize() != null ? directory.getTotalSize() + fileSize : fileSize);
        } else {
            directory.setFileCount(directory.getFileCount() != null ? Math.max(0, directory.getFileCount() - 1) : 0);
            directory.setTotalSize(directory.getTotalSize() != null ? Math.max(0, directory.getTotalSize() - fileSize) : 0);
        }
        updateByIdSelective(directory);
    }

    private String extractDirName(String dirPath) {
        if (dirPath == null || dirPath.isEmpty()) {
            return "";
        }
        dirPath = dirPath.endsWith("/") ? dirPath.substring(0, dirPath.length() - 1) : dirPath;
        int lastSlash = dirPath.lastIndexOf("/");
        return lastSlash >= 0 ? dirPath.substring(lastSlash + 1) : dirPath;
    }

    private String extractParentPath(String dirPath) {
        if (dirPath == null || dirPath.isEmpty()) {
            return null;
        }
        dirPath = dirPath.endsWith("/") ? dirPath.substring(0, dirPath.length() - 1) : dirPath;
        int lastSlash = dirPath.lastIndexOf("/");
        return lastSlash > 0 ? dirPath.substring(0, lastSlash) : null;
    }

    private int calculateDirLevel(String dirPath) {
        if (StringUtils.isBlank(dirPath)) {
            return 0;
        }
        String normalizedPath = dirPath.endsWith("/") ? dirPath.substring(0, dirPath.length() - 1) : dirPath;
        return (int) normalizedPath.chars().filter(c -> c == '/').count() + 1;
    }

}
