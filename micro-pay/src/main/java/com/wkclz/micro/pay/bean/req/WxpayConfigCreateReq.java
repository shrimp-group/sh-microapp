package com.wkclz.micro.pay.bean.req;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "微信支付配置创建请求")
public class WxpayConfigCreateReq {

    @Schema(description = "租户编码")
    private String tenantCode;

    @NotBlank(message = "appId 不能为空")
    @Schema(description = "AppId")
    private String appId;

    @NotBlank(message = "mchId 不能为空")
    @Schema(description = "支付商户号")
    private String mchId;

    @NotBlank(message = "mchV3Key 不能为空")
    @Schema(description = "支付商户密钥V3")
    private String mchV3Key;

    @NotBlank(message = "apiclientKey 不能为空")
    @Schema(description = "商户API证书Key")
    private String apiclientKey;

    @NotBlank(message = "apiclientCert 不能为空")
    @Schema(description = "商户API证书Cert")
    private String apiclientCert;

    @Schema(description = "商户API证书序列号")
    private String mchCertSerialNo;

    @NotBlank(message = "notifyUrl 不能为空")
    @Schema(description = "服务器异步通知路径")
    private String notifyUrl;

    @NotBlank(message = "returnUrl 不能为空")
    @Schema(description = "页面跳转同步通知页面路径")
    private String returnUrl;

    @NotBlank(message = "refundNotifyUrl 不能为空")
    @Schema(description = "退款回调地址")
    private String refundNotifyUrl;

    @Schema(description = "微信域名验证签名")
    private String verifySign;

    @Schema(description = "排序")
    private Integer sort;

    @Schema(description = "备注")
    private String remark;
}
