package com.wkclz.micro.wxmp.service;

import com.wkclz.core.base.PageData;
import com.wkclz.core.enums.ResultCode;
import com.wkclz.core.exception.ValidationException;
import com.wkclz.micro.wxmp.dao.WxmpConfigMapper;
import com.wkclz.micro.wxmp.pojo.entity.WxmpConfig;
import com.wkclz.micro.wxmp.pojo.vo.WxMpAppInfo;
import com.wkclz.mybatis.helper.PageQuery;
import com.wkclz.mybatis.service.BaseService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;


/**
 * Description Create by shrimp-gen
 * @author wangkaicun
 * @table wxmp_config (公众号) 单表服务类，代码重新生成不覆盖. 只建议完成单表的逻辑，或主表为 wxmp_config 的逻辑. 其他逻辑放 custom 中
 */
 
@Service
public class WxmpConfigService extends BaseService<WxmpConfig, WxmpConfigMapper> {

    public WxMpAppInfo getWxMpAppInfo(WxMpAppInfo param) {
        return mapper.getWxMpAppInfo(param);
    }

    public WxmpConfig getConfigByAppId(String appId) {
        if (StringUtils.isBlank(appId)) {
            throw ValidationException.of("appId 不能为空");
        }
        WxmpConfig entity = new WxmpConfig();
        entity.setAppId(appId);
        entity = mapper.selectOneByEntity(entity);
        if (entity == null) {
            throw ValidationException.of("appId 错误，不存在相关配置");
        }
        return entity;
    }

    public PageData<WxmpConfig> getConfigPage(WxmpConfig entity) {
        return PageQuery.page(entity, mapper::getConfigList);
    }

    public WxmpConfig getConfigInfo(WxmpConfig entity) {
        Assert.notNull(entity.getId(), ResultCode.PARAM_NO_ID.getMessage());
        Assert.notNull(entity.getTenantCode(), "请求错误！参数[tenantCode]不能为空");
        WxmpConfig config = selectOneByEntity(entity);
        if (config == null) {
            throw ValidationException.of(ResultCode.RECORD_NOT_EXIST);
        }
        if (StringUtils.isNotBlank(config.getAppSecret())) {
            config.setAppSecret("******");
        }
        if (StringUtils.isNotBlank(config.getMpToken())) {
            config.setMpToken("******");
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

    public WxmpConfig create(WxmpConfig entity) {
        duplicateCheck(entity);
        mapper.insert(entity);
        return entity;
    }

    public WxmpConfig update(WxmpConfig entity) {
        duplicateCheck(entity);
        Assert.notNull(entity.getId(), ResultCode.PARAM_NO_ID.getMessage());
        Assert.notNull(entity.getId(), ResultCode.UPDATE_NO_VERSION.getMessage());
        WxmpConfig oldEntity = selectById(entity.getId());
        if (oldEntity == null) {
            throw ValidationException.of(ResultCode.RECORD_NOT_EXIST);
        }
        if (StringUtils.isBlank(entity.getAppSecret()) || "******".equals(entity.getAppSecret())) {
            entity.setAppSecret(null);
        }
        if (StringUtils.isBlank(entity.getMpToken()) || "******".equals(entity.getMpToken())) {
            entity.setMpToken(null);
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
        WxmpConfig.copyIfNotNull(entity, oldEntity);
        updateByIdSelective(oldEntity);
        return oldEntity;
    }

    private void duplicateCheck(WxmpConfig entity) {
        // 唯一条件为空，直接通过
        if (StringUtils.isBlank(entity.getAppId())) {
            return;
        }
        
        // 唯一条件不为空，请设置唯一条件
        WxmpConfig param = new WxmpConfig();
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

