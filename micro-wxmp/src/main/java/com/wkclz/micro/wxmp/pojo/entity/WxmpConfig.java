package com.wkclz.micro.wxmp.pojo.entity;

import com.wkclz.core.annotation.Desc;
import com.wkclz.core.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;




/**
 * Description Create by shrimp-gen
 * @author wangkaicun
 * @table wxmp_config (公众号) 重新生成代码会覆盖
 */

@Data
@EqualsAndHashCode(callSuper = false)
public class WxmpConfig extends BaseEntity {

    /**
     * 租户编码
     */
    @Desc("租户编码")
    private String tenantCode;

    /**
     * 公众号appid
     */
    @Desc("公众号appid")
    private String appId;

    /**
     * 公众号Secret
     */
    @Desc("公众号Secret")
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
     * 公众号回调服务端的token
     */
    @Desc("公众号回调服务端的token")
    private String mpToken;

    /**
     * 公众号回调服务端的AESKey
     */
    @Desc("公众号回调服务端的AESKey")
    private String aesKey;

    /**
     * 公众号菜单数据
     */
    @Desc("公众号菜单数据")
    private String mpMenuJson;

    /**
     * 公众号-对话-回复映射 (default为默认回复)
     */
    @Desc("公众号-对话-回复映射 (default为默认回复)")
    private String mpTalkReplyMap;

    /**
     * 欢迎信息
     */
    @Desc("欢迎信息")
    private String welcomeMsg;


    public static WxmpConfig copy(WxmpConfig source, WxmpConfig target) {
        if (target == null ) { target = new WxmpConfig();}
        if (source == null) { return target; }
        target.setId(source.getId());
        target.setTenantCode(source.getTenantCode());
        target.setAppId(source.getAppId());
        target.setAppSecret(source.getAppSecret());
        target.setCertPem(source.getCertPem());
        target.setKeyPem(source.getKeyPem());
        target.setMpToken(source.getMpToken());
        target.setAesKey(source.getAesKey());
        target.setMpMenuJson(source.getMpMenuJson());
        target.setMpTalkReplyMap(source.getMpTalkReplyMap());
        target.setWelcomeMsg(source.getWelcomeMsg());
        target.setSort(source.getSort());
        target.setCreateTime(source.getCreateTime());
        target.setCreateBy(source.getCreateBy());
        target.setUpdateTime(source.getUpdateTime());
        target.setUpdateBy(source.getUpdateBy());
        target.setRemark(source.getRemark());
        target.setVersion(source.getVersion());
        return target;
    }

    public static WxmpConfig copyIfNotNull(WxmpConfig source, WxmpConfig target) {
        if (target == null ) { target = new WxmpConfig();}
        if (source == null) { return target; }
        if (source.getId() != null) { target.setId(source.getId()); }
        if (source.getTenantCode() != null) { target.setTenantCode(source.getTenantCode()); }
        if (source.getAppId() != null) { target.setAppId(source.getAppId()); }
        if (source.getAppSecret() != null) { target.setAppSecret(source.getAppSecret()); }
        if (source.getCertPem() != null) { target.setCertPem(source.getCertPem()); }
        if (source.getKeyPem() != null) { target.setKeyPem(source.getKeyPem()); }
        if (source.getMpToken() != null) { target.setMpToken(source.getMpToken()); }
        if (source.getAesKey() != null) { target.setAesKey(source.getAesKey()); }
        if (source.getMpMenuJson() != null) { target.setMpMenuJson(source.getMpMenuJson()); }
        if (source.getMpTalkReplyMap() != null) { target.setMpTalkReplyMap(source.getMpTalkReplyMap()); }
        if (source.getWelcomeMsg() != null) { target.setWelcomeMsg(source.getWelcomeMsg()); }
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

