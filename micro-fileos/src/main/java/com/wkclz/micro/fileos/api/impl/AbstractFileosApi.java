package com.wkclz.micro.fileos.api.impl;

import com.wkclz.core.exception.ValidationException;
import com.wkclz.micro.fileos.bean.entity.MdmFileosBucket;
import com.wkclz.micro.fileos.bean.enums.OssSpEnum;
import com.wkclz.micro.fileos.config.FileosConfig;
import com.wkclz.micro.fileos.helper.*;
import com.wkclz.micro.fileos.service.FileosService;
import com.wkclz.micro.fileos.service.MdmFileosRecordService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.EnumUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
public abstract class AbstractFileosApi {

    protected static final Pattern URI_PATTERN = Pattern.compile("https?://[^/]+(/[^?#]*)");

    @Autowired
    protected PathHelper pathHelper;
    @Autowired
    protected BucketCache bucketCache;
    @Autowired
    protected FileosConfig fileosConfig;
    @Autowired
    protected FileHashHelper fileHashHelper;
    @Autowired
    protected FileTypeHelper fileTypeHelper;
    @Autowired
    protected DirectoryHelper directoryHelper;
    @Autowired
    protected Map<String, FileosService> fileServiceMap;
    @Autowired
    protected MdmFileosRecordService mdmFileosRecordService;

    protected MdmFileosBucket getBucket(String bucketName) {
        if (StringUtils.isNotBlank(bucketName)) {
            MdmFileosBucket bucket = bucketCache.get(bucketName);
            if (bucket == null) {
                throw ValidationException.of("bucket: {} 未配置，请联系管理员检查！", bucketName);
            }
            return bucket;
        }
        MdmFileosBucket bucket = bucketCache.get();
        if (bucket == null) {
            throw ValidationException.of("无法匹配可用的 bucket 配置，请联系管理员完善 bucket 配置！");
        }
        return bucket;
    }

    protected FileosService getApi(MdmFileosBucket bucket) {
        if (bucket == null) {
            throw ValidationException.of("无 bucket 信息");
        }
        String ossSp = bucket.getOssSp();
        return getApi(ossSp);
    }

    protected FileosService getApi(String ossSp) {
        if (StringUtils.isBlank(ossSp)) {
            throw ValidationException.of("bucket 未维护 sp 信息，请联系管理员！");
        }
        if (!EnumUtils.isValidEnum(OssSpEnum.class, ossSp)) {
            throw ValidationException.of("bucket 未支持的 sp，请联系管理员！");
        }
        OssSpEnum anEnum = EnumUtils.getEnum(OssSpEnum.class, ossSp);
        FileosService service = fileServiceMap.get(anEnum.getServiceName());
        if (service == null) {
            throw ValidationException.of("未找到对应的文件服务实现: {}", anEnum.getServiceName());
        }
        return service;
    }

    protected static String getFileId(String orgStr) {
        if (StringUtils.isBlank(orgStr)) {
            return orgStr;
        }
        if (!orgStr.startsWith("http")) {
            return orgStr;
        }
        Matcher matcher = URI_PATTERN.matcher(orgStr);
        if (matcher.find()) {
            String group = matcher.group(1);
            return group.substring(1);
        }
        return orgStr;
    }

    protected static String getCategory(String category) {
        if (StringUtils.isNotBlank(category)) {
            if (category.contains("/") || category.contains("\\") || category.contains("..")) {
                throw ValidationException.of("业务分类包含非法字符: " + category);
            }
            return category;
        }
        return "common";
    }

    protected void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw ValidationException.of("上传文件不能为空");
        }
        String originalFilename = file.getOriginalFilename();
        long size = file.getSize();

        String extName = FileTypeHelper.getExtName(originalFilename);

        if (fileTypeHelper.isImage(originalFilename)) {
            if (!isExtensionAllowed(extName, fileosConfig.getImageExtensionNames())) {
                log.warn("图片扩展名不在白名单中, 文件名: {}, 扩展名: {}", originalFilename, extName);
                throw ValidationException.of("不支持的文件类型: " + extName);
            }
            Integer imageMaxSizeMb = fileosConfig.getImageMaxSizeMb();
            if (size > imageMaxSizeMb * 1024L * 1024) {
                throw ValidationException.of("上传图片不能超过 {}Mb", imageMaxSizeMb);
            }
        } else if (fileTypeHelper.isVideo(originalFilename)) {
            if (!isExtensionAllowed(extName, fileosConfig.getVideoExtensionNames())) {
                log.warn("视频扩展名不在白名单中, 文件名: {}, 扩展名: {}", originalFilename, extName);
                throw ValidationException.of("不支持的文件类型: " + extName);
            }
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

        if (!fileTypeHelper.validateFileContent(file)) {
            log.warn("文件内容校验失败，文件名: {}", originalFilename);
            throw ValidationException.of("文件内容与扩展名不匹配，请检查文件是否合法");
        }
    }

    private boolean isExtensionAllowed(String extName, String extensionNames) {
        if (StringUtils.isBlank(extName) || StringUtils.isBlank(extensionNames)) {
            return false;
        }
        List<String> allowed = Arrays.asList(extensionNames.toLowerCase().split("[,，;；|]"));
        return allowed.contains(extName);
    }

    protected void ensureDirectoryAsync(String fileId, String bucketName, String tenantCode, long fileSize) {
        directoryHelper.ensureDirectoryAsync(fileId, bucketName, tenantCode, fileSize);
    }

}
