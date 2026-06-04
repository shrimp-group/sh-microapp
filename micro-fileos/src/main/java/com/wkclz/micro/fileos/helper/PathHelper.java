package com.wkclz.micro.fileos.helper;

import cn.hutool.core.date.DateUtil;
import com.wkclz.micro.fileos.bean.FileosConstant;
import com.wkclz.micro.fileos.bean.entity.MdmFileosDirectory;
import com.wkclz.micro.fileos.config.FileosConfig;
import com.wkclz.micro.fileos.utils.OssUtil;
import com.wkclz.redis.helper.RedisIdGenerator;
import com.wkclz.spring.config.Sys;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Slf4j
@Component
public class PathHelper {

    private static volatile long lastTimestamp = 0;

    @Autowired
    private BucketCache bucketCache;
    @Autowired
    private FileosConfig fileosConfig;
    @Autowired
    private RedisIdGenerator redisIdGenerator;

    public String getFullName(String category, String filename, Boolean isPublic) {
        String system = fileosConfig.getSystem();

        String env = Sys.getCurrentEnv().toString().toLowerCase();

        if (StringUtils.isBlank(category)) {
            category = "common";
        }
        category = category.toLowerCase();

        String day = DateUtil.format(new Date(), "yyyyMMdd");
        String timestamp = DateUtil.format(new Date(), "yyyyMMddHHmmssSSS");

        long currentMs = System.currentTimeMillis();
        if (currentMs != lastTimestamp) {
            lastTimestamp = currentMs;
        }

        String safeFilename = OssUtil.sanitizeFileName(filename);
        if (safeFilename != null) {
            safeFilename = timestamp + "_" + safeFilename;
        } else {
            safeFilename = timestamp;
        }

        StringBuilder path = new StringBuilder();
        path.append(system).append("/");
        path.append(env).append("/");
        path.append(category).append("/");
        path.append(day).append("/");

        if (isPublic != null && isPublic) {
            path.append(FileosConstant.PUBLIC_PREFIX);
        }

        path.append(safeFilename);
        return path.toString();
    }

    public String extractDirPath(String fileId) {
        if (StringUtils.isBlank(fileId)) {
            return null;
        }
        int lastSlash = fileId.lastIndexOf("/");
        if (lastSlash == -1) {
            return null;
        }
        return fileId.substring(0, lastSlash);
    }

    public List<MdmFileosDirectory> extractDirectoryLevels(String fileId, String bucketName) {
        List<MdmFileosDirectory> directories = new ArrayList<>();
        if (StringUtils.isBlank(fileId)) {
            return directories;
        }

        String dirPath = extractDirPath(fileId);
        if (StringUtils.isBlank(dirPath)) {
            return directories;
        }

        String[] parts = dirPath.split("/");
        StringBuilder currentPath = new StringBuilder();

        for (int i = 0; i < parts.length; i++) {
            if (i > 0) {
                currentPath.append("/");
            }
            currentPath.append(parts[i]);

            MdmFileosDirectory dir = new MdmFileosDirectory();
            dir.setBucketName(bucketName);
            dir.setDirPath(currentPath.toString());
            dir.setDirName(parts[i]);
            dir.setDirLevel(i + 1);
            if (i > 0) {
                int parentEnd = currentPath.lastIndexOf("/" + parts[i]);
                dir.setParentPath(currentPath.substring(0, parentEnd));
            } else {
                dir.setParentPath("");
            }
            directories.add(dir);
        }

        return directories;
    }

}
