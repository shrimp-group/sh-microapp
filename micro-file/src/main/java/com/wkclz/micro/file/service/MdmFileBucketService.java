package com.wkclz.micro.file.service;

import com.wkclz.core.base.PageData;
import com.wkclz.core.exception.ValidationException;
import com.wkclz.iam.sdk.helper.SessionHelper;
import com.wkclz.micro.file.bean.entity.MdmFileBucket;
import com.wkclz.micro.file.helper.BucketCache;
import com.wkclz.micro.file.mapper.MdmFileBucketMapper;
import com.wkclz.mybatis.helper.PageQuery;
import com.wkclz.mybatis.service.BaseService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MdmFileBucketService extends BaseService<MdmFileBucket, MdmFileBucketMapper> {

    @Autowired
    private BucketCache bucketCache;

    public PageData getPage(MdmFileBucket entity) {
        entity.setTenantCode(SessionHelper.getTenantCode());
        PageData<MdmFileBucket> page = PageQuery.page(entity, mapper::getBucketList);
        if (page.getRecords() != null) {
            page.getRecords().forEach(b -> {
                b.setAccessKey(maskSensitive(b.getAccessKey()));
                b.setSecretKey("******");
            });
        }
        return page;
    }

    public MdmFileBucket getInfo(MdmFileBucket entity) {
        entity.setTenantCode(SessionHelper.getTenantCode());
        entity = selectOneByEntity(entity);
        if (entity == null) {
            throw ValidationException.of("bucket 不存在或无权操作");
        }
        entity.setAccessKey(maskSensitive(entity.getAccessKey()));
        if (StringUtils.isNotBlank(entity.getSecretKey())) {
            entity.setSecretKey("******");
        }
        return entity;
    }

    public MdmFileBucket update(MdmFileBucket entity) {
        entity.setTenantCode(SessionHelper.getTenantCode());
        if (StringUtils.isBlank(entity.getSecretKey())) {
            MdmFileBucket existing = selectById(entity.getId());
            if (existing != null) {
                entity.setSecretKey(existing.getSecretKey());
            }
        }
        updateByIdSelective(entity);
        bucketCache.clearCache();
        return entity;
    }

    public Integer remove(MdmFileBucket entity) {
        entity.setTenantCode(SessionHelper.getTenantCode());
        Integer result = deleteById(entity);
        bucketCache.clearCache();
        return result;
    }

    public List<MdmFileBucket> getBucketOptions(MdmFileBucket entity) {
        entity.setTenantCode(SessionHelper.getTenantCode());
        return mapper.getBucketOptions(entity);
    }

    private static String maskSensitive(String value) {
        if (StringUtils.isBlank(value) || value.length() <= 4) {
            return "****";
        }
        return value.substring(0, 2) + "****" + value.substring(value.length() - 2);
    }

}
