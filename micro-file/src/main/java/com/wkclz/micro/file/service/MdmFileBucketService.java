package com.wkclz.micro.file.service;

import com.wkclz.core.base.PageData;
import com.wkclz.core.enums.ResultCode;
import com.wkclz.core.exception.UserException;
import com.wkclz.core.exception.ValidationException;
import com.wkclz.iam.sdk.helper.SessionHelper;
import com.wkclz.micro.file.mapper.MdmFileBucketMapper;
import com.wkclz.micro.file.pojo.entity.MdmFileBucket;
import com.wkclz.mybatis.helper.PageQuery;
import com.wkclz.mybatis.service.BaseService;
import com.wkclz.tool.utils.BeanUtil;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Description Create by sh-generator
 * @author shrimp
 * @table mdm_file_bucket (Bucket管理) 单表服务类，代码重新生成不覆盖. 只建议完成单表的逻辑，或主表为 mdm_file_bucket 的逻辑. 其他逻辑放 custom 中
 */
 
@Service
public class MdmFileBucketService extends BaseService<MdmFileBucket, MdmFileBucketMapper> {

    public PageData getPage(MdmFileBucket entity) {
        entity.setTenantCode(SessionHelper.getTenantCode());
        return PageQuery.page(entity, mapper::getBucketList);
    }

    public MdmFileBucket getInfo(MdmFileBucket entity) {
        entity.setTenantCode(SessionHelper.getTenantCode());
        entity = selectOneByEntity(entity);
        if (entity == null) {
            throw ValidationException.of("bucket 不存在或无权操作");
        }
        if (StringUtils.isNotBlank(entity.getSecretKey())) {
            entity.setSecretKey("******");
        }
        return entity;
    }

    @Transactional(rollbackFor = Exception.class)
    public MdmFileBucket create(MdmFileBucket entity) {
        duplicateCheck(entity);
        checkAndMakeDefault(entity);
        insert(entity);
        entity.setSecretKey(null);
        return entity;
    }

    @Transactional(rollbackFor = Exception.class)
    public MdmFileBucket update(MdmFileBucket entity) {
        duplicateCheck(entity);
        checkAndMakeDefault(entity);
        MdmFileBucket mdmFileBucket = selectById(entity.getId());
        if (mdmFileBucket == null) {
            throw ValidationException.of("错误的 id, 待更新的数据不存在！");
        }
        if (StringUtils.isBlank(entity.getSecretKey()) || "******".equals(entity.getSecretKey())) {
            entity.setSecretKey(null);
        }
        BeanUtil.cp(entity, mdmFileBucket, false);
        updateById(mdmFileBucket);
        mdmFileBucket.setSecretKey(null);
        return mdmFileBucket;
    }

    @Transactional(rollbackFor = Exception.class)
    public Integer remove(MdmFileBucket entity) {
        entity.setTenantCode(SessionHelper.getTenantCode());
        MdmFileBucket mdmFileBucket = selectById(entity.getId());
        if (mdmFileBucket == null) {
            throw ValidationException.of("bucket 不存在或无权操作");
        }

        deleteById(entity);

        if (mdmFileBucket.getDefaultFlag() == 1) {
            // 重新选定默认 bucket
            mdmFileBucket.setDefaultFlag(0);
            checkAndMakeDefault(mdmFileBucket);
        }
        return 1;
    }


    public List<MdmFileBucket> getBucketOptions(MdmFileBucket entity) {
        entity.setTenantCode(SessionHelper.getTenantCode());
        return mapper.getBucketOptions(entity);
    }


    private void checkAndMakeDefault(MdmFileBucket entity) {
        if (entity == null) {
            return;
        }

        MdmFileBucket param = new MdmFileBucket();
        param.setTenantCode(SessionHelper.getTenantCode());
        param.setDefaultFlag(1);

        if (entity.getDefaultFlag() == 1) {
            // 当前操作的为默认 bucket, 取消其他
            List<MdmFileBucket> list = selectByEntity(param);
            if (entity.getId() != null) {
                list = list.stream().filter(t -> !t.getId().equals(entity.getId())).collect(Collectors.toList());
            }
            if (CollectionUtils.isEmpty(list)) {
                return;
            }
            list.forEach(t -> {
                t.setDefaultFlag(0);
                updateById(t);
            });
        }
        if (entity.getDefaultFlag() == 0) {
            // 确认是否有默认
            // 有默认，跳过
            List<MdmFileBucket> list = selectByEntity(param);
            if (entity.getId() != null) {
                list = list.stream().filter(t -> !t.getId().equals(entity.getId())).toList();
            }
            if (!list.isEmpty()) {
                return;
            }
            // 没有默认，强制默认
            entity.setDefaultFlag(1);
        }
    }


    public void duplicateCheck(MdmFileBucket entity) {
        if (entity == null) {
            return;
        }
        if (StringUtils.isBlank(entity.getBucket())) {
            return;
        }
        MdmFileBucket param = new MdmFileBucket();
        param.setBucket(entity.getBucket());
        param = selectOneByEntity(param);
        if (param == null) {
            return;
        }
        if (entity.getId() != null && entity.getId().equals(param.getId())) {
            return;
        }
        throw ValidationException.of("重复的 bucket");
    }



}

