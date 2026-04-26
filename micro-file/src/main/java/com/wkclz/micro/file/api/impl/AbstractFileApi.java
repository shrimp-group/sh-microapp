package com.wkclz.micro.file.api.impl;

import com.wkclz.core.exception.ValidationException;
import com.wkclz.micro.file.bean.entity.MdmFileBucket;
import com.wkclz.micro.file.bean.enums.OssSpEnum;
import com.wkclz.micro.file.helper.BucketCache;
import com.wkclz.micro.file.service.FileService;
import com.wkclz.micro.file.service.MdmFileRecordService;
import org.apache.commons.lang3.EnumUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class AbstractFileApi {

    protected static final Pattern URI_PATTERN = Pattern.compile("https?://[^/]+(/[^?#]*)");

    @Autowired
    protected BucketCache bucketCache;
    @Autowired
    protected Map<String, FileService> fileServiceMap;
    @Autowired
    protected MdmFileRecordService mdmFileRecordService;

    protected static String getBusinessType(String businessType) {
        if (StringUtils.isNotBlank(businessType)) {
            return businessType;
        }
        return "common";
    }

    protected MdmFileBucket getBucket(String bucket) {
        if (StringUtils.isNotBlank(bucket)) {
            MdmFileBucket mdmFileBucket = bucketCache.get(bucket);
            if (mdmFileBucket == null) {
                throw ValidationException.of("bucket: {} 未配置，请联系管理员检查！");
            }
            return mdmFileBucket;
        }
        MdmFileBucket mdmFileBucket = bucketCache.get();
        if (mdmFileBucket == null) {
            throw ValidationException.of("无法匹配可用的 bucket 配置，请联系管理员完善 bucket 配置！");
        }
        return mdmFileBucket;
    }

    protected FileService getApi(MdmFileBucket bucket) {
        if (bucket == null) {
            throw ValidationException.of("无 bucket 信息");
        }
        String ossSp = bucket.getOssSp();
        return getApi(ossSp);
    }

    protected FileService getApi(String ossSp) {
        if (StringUtils.isBlank(ossSp)) {
            throw ValidationException.of("bucket 未维护 sp 信息，请联系管理员！");
        }
        if (!EnumUtils.isValidEnum(OssSpEnum.class, ossSp)) {
            throw ValidationException.of("bucket 未支持的 sp，请联系管理员！");
        }
        OssSpEnum anEnum = EnumUtils.getEnum(OssSpEnum.class, ossSp);
        FileService service = fileServiceMap.get(anEnum.getServiceName());
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

}
