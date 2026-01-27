package com.wkclz.micro.pay.pojo.entity;

import com.wkclz.core.annotation.Desc;
import com.wkclz.core.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;




/**
 * Description Create by shrimp-gen
 * @author wangkaicun
 * @table pay_wxpay_config (支付-微信支付配置) 重新生成代码会覆盖
 */

@Data
@EqualsAndHashCode(callSuper = false)
public class PayWxpayConfig extends BaseEntity {

    /**
     * 租户编码
     */
    @Desc("租户编码")
    private String tenantCode;

    /**
     * AppId
     */
    @Desc("AppId")
    private String appId;

    /**
     * 支付商户号
     */
    @Desc("支付商户号")
    private String mchId;

    /**
     * 支付商户密钥V3
     */
    @Desc("支付商户密钥V3")
    private String mchV3Key;

    /**
     * 商户API证书Key
     */
    @Desc("商户API证书Key")
    private String apiclientKey;

    /**
     * 商户API证书Cert
     */
    @Desc("商户API证书Cert")
    private String apiclientCert;

    /**
     * 商户API证书序列号
     */
    @Desc("商户API证书序列号")
    private String mchCertSerialNo;

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
     * 退款回调地址
     */
    @Desc("退款回调地址")
    private String refundNotifyUrl;

    /**
     * 微信域名验证签名
     */
    @Desc("微信域名验证签名")
    private String verifySign;


    public static PayWxpayConfig copy(PayWxpayConfig source, PayWxpayConfig target) {
        if (target == null ) { target = new PayWxpayConfig();}
        if (source == null) { return target; }
        target.setId(source.getId());
        target.setTenantCode(source.getTenantCode());
        target.setAppId(source.getAppId());
        target.setMchId(source.getMchId());
        target.setMchV3Key(source.getMchV3Key());
        target.setApiclientKey(source.getApiclientKey());
        target.setApiclientCert(source.getApiclientCert());
        target.setMchCertSerialNo(source.getMchCertSerialNo());
        target.setNotifyUrl(source.getNotifyUrl());
        target.setReturnUrl(source.getReturnUrl());
        target.setRefundNotifyUrl(source.getRefundNotifyUrl());
        target.setVerifySign(source.getVerifySign());
        target.setSort(source.getSort());
        target.setCreateTime(source.getCreateTime());
        target.setCreateBy(source.getCreateBy());
        target.setUpdateTime(source.getUpdateTime());
        target.setUpdateBy(source.getUpdateBy());
        target.setRemark(source.getRemark());
        target.setVersion(source.getVersion());
        return target;
    }

    public static PayWxpayConfig copyIfNotNull(PayWxpayConfig source, PayWxpayConfig target) {
        if (target == null ) { target = new PayWxpayConfig();}
        if (source == null) { return target; }
        if (source.getId() != null) { target.setId(source.getId()); }
        if (source.getTenantCode() != null) { target.setTenantCode(source.getTenantCode()); }
        if (source.getAppId() != null) { target.setAppId(source.getAppId()); }
        if (source.getMchId() != null) { target.setMchId(source.getMchId()); }
        if (source.getMchV3Key() != null) { target.setMchV3Key(source.getMchV3Key()); }
        if (source.getApiclientKey() != null) { target.setApiclientKey(source.getApiclientKey()); }
        if (source.getApiclientCert() != null) { target.setApiclientCert(source.getApiclientCert()); }
        if (source.getMchCertSerialNo() != null) { target.setMchCertSerialNo(source.getMchCertSerialNo()); }
        if (source.getNotifyUrl() != null) { target.setNotifyUrl(source.getNotifyUrl()); }
        if (source.getReturnUrl() != null) { target.setReturnUrl(source.getReturnUrl()); }
        if (source.getRefundNotifyUrl() != null) { target.setRefundNotifyUrl(source.getRefundNotifyUrl()); }
        if (source.getVerifySign() != null) { target.setVerifySign(source.getVerifySign()); }
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

