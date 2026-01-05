package com.wkclz.micro.wxapp.service;

import cn.hutool.core.lang.Assert;
import com.wkclz.core.base.PageData;
import com.wkclz.core.enums.ResultCode;
import com.wkclz.core.exception.ValidationException;
import com.wkclz.micro.wxapp.bean.entity.WxappConfig;
import com.wkclz.micro.wxapp.bean.vo.WxMaAppInfo;
import com.wkclz.micro.wxapp.mapper.WxappConfigMapper;
import com.wkclz.mybatis.helper.PageQuery;
import com.wkclz.mybatis.service.BaseService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

/**
 * Description Create by shrimp-gen
 * @author wangkaicun
 * @table wxapp_config (小程序) 单表服务类，代码重新生成不覆盖. 只建议完成单表的逻辑，或主表为 wxapp_config 的逻辑. 其他逻辑放 custom 中
 */
 
@Service
public class WxappConfigService extends BaseService<WxappConfig, WxappConfigMapper> {

    public WxMaAppInfo getWxMaAppInfo(WxMaAppInfo param) {
        return mapper.getWxMaAppInfo(param);
    }


    public PageData<WxappConfig> getConfigPage(WxappConfig entity) {
        return PageQuery.page(entity, mapper::getConfigList);
    }

    public WxappConfig getConfigInfo(WxappConfig entity) {
        Assert.notNull(entity.getId(), "请求错误！参数[id]不能为空");
        Assert.notNull(entity.getTenantCode(), "请求错误！参数[tenantCode]不能为空");
        WxappConfig config = selectOneByEntity(entity);
        if (config == null) {
            throw ValidationException.of(ResultCode.RECORD_NOT_EXIST);
        }
        if (StringUtils.isNotBlank(config.getAppSecret())) {
            config.setAppSecret("******");
        }
        if (StringUtils.isNotBlank(config.getAppToken())) {
            config.setAppToken("******");
        }
        if (StringUtils.isNotBlank(config.getAesKey())) {
            config.setAesKey("******");
        }
        if (StringUtils.isNotBlank(config.getCertPem())) {
            config.setCertPem("******");
        }
        if (StringUtils.isNotBlank(config.getKeyPem())) {
            config.setKeyPem("******");
        }
        return config;
    }

    public WxappConfig create(WxappConfig entity) {
        duplicateCheck(entity);
        mapper.insert(entity);
        return entity;
    }

    public WxappConfig update(WxappConfig entity) {
        duplicateCheck(entity);
        Assert.notNull(entity.getId(), "请求错误！参数[id]不能为空");
        Assert.notNull(entity.getVersion(), "请求错误！参数[version]不能为空");
        WxappConfig oldEntity = selectById(entity.getId());
        if (oldEntity == null) {
            throw ValidationException.of(ResultCode.RECORD_NOT_EXIST);
        }

        if (StringUtils.isBlank(entity.getAppSecret()) || "******".equals(entity.getAppSecret())) {
            entity.setAppSecret(null);
        }
        if (StringUtils.isBlank(entity.getAppToken()) || "******".equals(entity.getAppToken())) {
            entity.setAppToken(null);
        }
        if (StringUtils.isBlank(entity.getAesKey()) || "******".equals(entity.getAesKey())) {
            entity.setAesKey(null);
        }
        if (StringUtils.isBlank(entity.getCertPem()) || "******".equals(entity.getCertPem())) {
            entity.setCertPem(null);
        }
        if (StringUtils.isBlank(entity.getKeyPem()) || "******".equals(entity.getKeyPem())) {
            entity.setKeyPem(null);
        }
        WxappConfig.copyIfNotNull(entity, oldEntity);
        updateByIdSelective(oldEntity);
        return oldEntity;
    }

    private void duplicateCheck(WxappConfig entity) {
        Assert.notNull(entity);
        // 唯一条件为空，直接通过
        if (StringUtils.isBlank(entity.getAppId())) {
            return;
        }

        // 唯一条件不为空，请设置唯一条件
        WxappConfig param = new WxappConfig();
        param.setAppId(entity.getAppId());
        // 唯一条件
        param = selectOneByEntity(param);
        if (param == null) {
            return;
        }
        if (param.getId().equals(entity.getId())) {
            return;
        }
        // 查到有值，为新增或 id 不一样场景，为数据重复
        throw ValidationException.of(ResultCode.RECORD_DUPLICATE);
    }

}

