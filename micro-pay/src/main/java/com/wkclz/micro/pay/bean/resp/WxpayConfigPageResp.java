package com.wkclz.micro.pay.bean.resp;

import com.wkclz.web.bean.EntityResp;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "微信支付配置分页响应")
public class WxpayConfigPageResp extends EntityResp {

    @Schema(description = "租户编码")
    private String tenantCode;

    @Schema(description = "AppId")
    private String appId;

    @Schema(description = "支付商户号")
    private String mchId;

    @Schema(description = "支付商户密钥V3")
    private String mchV3Key;

    @Schema(description = "商户API证书Key")
    private String apiclientKey;

    @Schema(description = "商户API证书Cert")
    private String apiclientCert;

    @Schema(description = "商户API证书序列号")
    private String mchCertSerialNo;

    @Schema(description = "服务器异步通知路径")
    private String notifyUrl;

    @Schema(description = "页面跳转同步通知页面路径")
    private String returnUrl;

    @Schema(description = "退款回调地址")
    private String refundNotifyUrl;

    @Schema(description = "微信域名验证签名")
    private String verifySign;

    @Schema(description = "租户名称")
    private String tenantName;
}
