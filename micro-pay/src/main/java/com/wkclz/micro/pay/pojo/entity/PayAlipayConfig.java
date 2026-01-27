package com.wkclz.micro.pay.pojo.entity;

import com.wkclz.core.annotation.Desc;
import com.wkclz.core.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;




/**
 * Description Create by shrimp-gen
 * @author wangkaicun
 * @table pay_alipay_config (支付-支付宝配置) 重新生成代码会覆盖
 */

@Data
@EqualsAndHashCode(callSuper = false)
public class PayAlipayConfig extends BaseEntity {

    /**
     * 租户编码
     */
    @Desc("租户编码")
    private String tenantCode;

    /**
     * 应用ID
     */
    @Desc("应用ID")
    private String appId;

    /**
     * 商户私钥
     */
    @Desc("商户私钥")
    private String merchantPrivateKey;

    /**
     * 支付宝公钥
     */
    @Desc("支付宝公钥")
    private String alipayPublicKey;

    /**
     * 应用公钥
     */
    @Desc("应用公钥")
    private String appPublicKey;

    /**
     * 服务器异步通知路径
     */
    @Desc("服务器异步通知路径")
    private String notifyUrl;

    /**
     * 页面跳转同步通知页面路径
     */
    @Desc("页面跳转同步通知页面路径")
    private String returnUrl;

    /**
     * 签名方式
     */
    @Desc("签名方式")
    private String signType;

    /**
     * 字符编码格式
     */
    @Desc("字符编码格式")
    private String charset;

    /**
     * 是否为生产环境
     */
    @Desc("是否为生产环境")
    private Integer isProd;


    public static PayAlipayConfig copy(PayAlipayConfig source, PayAlipayConfig target) {
        if (target == null ) { target = new PayAlipayConfig();}
        if (source == null) { return target; }
        target.setId(source.getId());
        target.setTenantCode(source.getTenantCode());
        target.setAppId(source.getAppId());
        target.setMerchantPrivateKey(source.getMerchantPrivateKey());
        target.setAlipayPublicKey(source.getAlipayPublicKey());
        target.setAppPublicKey(source.getAppPublicKey());
        target.setNotifyUrl(source.getNotifyUrl());
        target.setReturnUrl(source.getReturnUrl());
        target.setSignType(source.getSignType());
        target.setCharset(source.getCharset());
        target.setIsProd(source.getIsProd());
        target.setSort(source.getSort());
        target.setCreateTime(source.getCreateTime());
        target.setCreateBy(source.getCreateBy());
        target.setUpdateTime(source.getUpdateTime());
        target.setUpdateBy(source.getUpdateBy());
        target.setRemark(source.getRemark());
        target.setVersion(source.getVersion());
        return target;
    }

    public static PayAlipayConfig copyIfNotNull(PayAlipayConfig source, PayAlipayConfig target) {
        if (target == null ) { target = new PayAlipayConfig();}
        if (source == null) { return target; }
        if (source.getId() != null) { target.setId(source.getId()); }
        if (source.getTenantCode() != null) { target.setTenantCode(source.getTenantCode()); }
        if (source.getAppId() != null) { target.setAppId(source.getAppId()); }
        if (source.getMerchantPrivateKey() != null) { target.setMerchantPrivateKey(source.getMerchantPrivateKey()); }
        if (source.getAlipayPublicKey() != null) { target.setAlipayPublicKey(source.getAlipayPublicKey()); }
        if (source.getAppPublicKey() != null) { target.setAppPublicKey(source.getAppPublicKey()); }
        if (source.getNotifyUrl() != null) { target.setNotifyUrl(source.getNotifyUrl()); }
        if (source.getReturnUrl() != null) { target.setReturnUrl(source.getReturnUrl()); }
        if (source.getSignType() != null) { target.setSignType(source.getSignType()); }
        if (source.getCharset() != null) { target.setCharset(source.getCharset()); }
        if (source.getIsProd() != null) { target.setIsProd(source.getIsProd()); }
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

