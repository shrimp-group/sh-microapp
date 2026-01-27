package com.wkclz.micro.file.service;

import com.wkclz.core.base.PageData;
import com.wkclz.core.exception.ValidationException;
import com.wkclz.iam.sdk.helper.SessionHelper;
import com.wkclz.micro.file.dao.MdmFsBucketMapper;
import com.wkclz.micro.file.pojo.entity.MdmFsBucket;
import com.wkclz.mybatis.helper.PageQuery;
import com.wkclz.mybatis.service.BaseService;
import com.wkclz.tool.utils.BeanUtil;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Description Create by shrimp-gen
 * @author wangkaicun
 * @table mdm_fs_bucket (Bucket管理) 单表服务类，代码重新生成不覆盖. 只建议完成单表的逻辑，或主表为 mdm_fs_bucket 的逻辑. 其他逻辑放 custom 中
 */
 
@Service
public class MdmFsBucketService extends BaseService<MdmFsBucket, MdmFsBucketMapper> {


    public PageData getPage(MdmFsBucket entity) {
        entity.setTenantCode(SessionHelper.getTenantCode());
        return PageQuery.page(entity, mapper::getBucketList);
    }

    public MdmFsBucket getInfo(MdmFsBucket entity) {
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
    public MdmFsBucket create(MdmFsBucket entity) {
        duplicateCheck(entity);
        checkAndMakeDefault(entity);
        insert(entity);
        entity.setSecretKey(null);
        return entity;
    }

    @Transactional(rollbackFor = Exception.class)
    public MdmFsBucket update(MdmFsBucket entity) {
        duplicateCheck(entity);
        checkAndMakeDefault(entity);
        MdmFsBucket mdmFsBucket = selectById(entity.getId());
        if (mdmFsBucket == null) {
            throw ValidationException.of("错误的 id, 待更新的数据不存在！");
        }
        if (StringUtils.isBlank(entity.getSecretKey()) || "******".equals(entity.getSecretKey())) {
            entity.setSecretKey(null);
        }
        BeanUtil.cp(entity, mdmFsBucket, false);
        updateById(mdmFsBucket);
        mdmFsBucket.setSecretKey(null);
        return mdmFsBucket;
    }

    @Transactional(rollbackFor = Exception.class)
    public Integer remove(MdmFsBucket entity) {
        entity.setTenantCode(SessionHelper.getTenantCode());
        MdmFsBucket mdmFsBucket = selectById(entity.getId());
        if (mdmFsBucket == null) {
            throw ValidationException.of("bucket 不存在或无权操作");
        }

        deleteById(entity);

        if (mdmFsBucket.getDefaultFlag() == 1) {
            // 重新选定默认 bucket
            mdmFsBucket.setDefaultFlag(0);
            checkAndMakeDefault(mdmFsBucket);
        }
        return 1;
    }


    public List<MdmFsBucket> getBucketOptions(MdmFsBucket entity) {
        entity.setTenantCode(SessionHelper.getTenantCode());
        return mapper.getBucketOptions(entity);
    }


    private void checkAndMakeDefault(MdmFsBucket entity) {
        if (entity == null) {
            return;
        }

        MdmFsBucket param = new MdmFsBucket();
        param.setTenantCode(SessionHelper.getTenantCode());
        param.setDefaultFlag(1);

        if (entity.getDefaultFlag() == 1) {
            // 当前操作的为默认 bucket, 取消其他
            List<MdmFsBucket> list = selectByEntity(param);
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
            List<MdmFsBucket> list = selectByEntity(param);
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


    public void duplicateCheck(MdmFsBucket entity) {
        if (entity == null) {
            return;
        }
        if (StringUtils.isBlank(entity.getBucket())) {
            return;
        }
        MdmFsBucket param = new MdmFsBucket();
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

