package com.wkclz.micro.wxapp.bean.entity;

import com.wkclz.core.annotation.Desc;
import com.wkclz.core.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;




/**
 * Description Create by shrimp-gen
 * @author wangkaicun
 * @table wxapp_config (小程序) 重新生成代码会覆盖
 */

@Data
@EqualsAndHashCode(callSuper = false)
public class WxappConfig extends BaseEntity {

    /**
     * 租户编码
     */
    @Desc("租户编码")
    private String tenantCode;

    /**
     * 小程序appid
     */
    @Desc("小程序appid")
    private String appId;

    /**
     * 小程序Secret
     */
    @Desc("小程序Secret")
    private String appSecret;

    /**
     * 证书文件cert
     */
    @Desc("证书文件cert")
    private String certPem;

    /**
     * 证书文件key
     */
    @Desc("证书文件key")
    private String keyPem;

    /**
     * 小程序消息服务器配置token
     */
    @Desc("小程序消息服务器配置token")
    private String appToken;

    /**
     * 小程序消息服务器配置EncodingAESKey
     */
    @Desc("小程序消息服务器配置EncodingAESKey")
    private String aesKey;


    public static WxappConfig copy(WxappConfig source, WxappConfig target) {
        if (target == null ) { target = new WxappConfig();}
        if (source == null) { return target; }
        target.setId(source.getId());
        target.setTenantCode(source.getTenantCode());
        target.setAppId(source.getAppId());
        target.setAppSecret(source.getAppSecret());
        target.setCertPem(source.getCertPem());
        target.setKeyPem(source.getKeyPem());
        target.setAppToken(source.getAppToken());
        target.setAesKey(source.getAesKey());
        target.setSort(source.getSort());
        target.setCreateTime(source.getCreateTime());
        target.setCreateBy(source.getCreateBy());
        target.setUpdateTime(source.getUpdateTime());
        target.setUpdateBy(source.getUpdateBy());
        target.setRemark(source.getRemark());
        target.setVersion(source.getVersion());
        return target;
    }

    public static WxappConfig copyIfNotNull(WxappConfig source, WxappConfig target) {
        if (target == null ) { target = new WxappConfig();}
        if (source == null) { return target; }
        if (source.getId() != null) { target.setId(source.getId()); }
        if (source.getTenantCode() != null) { target.setTenantCode(source.getTenantCode()); }
        if (source.getAppId() != null) { target.setAppId(source.getAppId()); }
        if (source.getAppSecret() != null) { target.setAppSecret(source.getAppSecret()); }
        if (source.getCertPem() != null) { target.setCertPem(source.getCertPem()); }
        if (source.getKeyPem() != null) { target.setKeyPem(source.getKeyPem()); }
        if (source.getAppToken() != null) { target.setAppToken(source.getAppToken()); }
        if (source.getAesKey() != null) { target.setAesKey(source.getAesKey()); }
        if (source.getSort() != null) { target.setSort(source.getSort()); }
        if (source.getCreateTime() != null) { target.setCreateTime(source.getCreateTime()); }
        if (source.getCreateBy() != null) { target.setCreateBy(source.getCreateBy()); }
        if (source.getUpdateTime() != null) { target.setUpdateTime(source.getUpdateTime()); }
        if (source.getUpdateBy() != null) { target.setUpdateBy(source.getUpdateBy()); }
        if (source.getRemark() != null) { target.setRemark(source.getRemark()); }
        if (source.getVersion() != null) { target.setVersion(source.getVersion()); }
        return target;
    }

}

