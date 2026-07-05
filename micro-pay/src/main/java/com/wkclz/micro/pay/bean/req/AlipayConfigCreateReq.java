package com.wkclz.micro.pay.bean.req;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "支付宝配置创建请求")
public class AlipayConfigCreateReq {

    @Schema(description = "租户编码")
    private String tenantCode;

    @NotBlank(message = "appId 不能为空！")
    @Schema(description = "应用ID")
    private String appId;

    @Schema(description = "商户私钥")
    private String merchantPrivateKey;

    @NotBlank(message = "alipayPublicKey 不能为空")
    @Schema(description = "支付宝公钥")
    private String alipayPublicKey;

    @NotBlank(message = "appPublicKey 不能为空")
    @Schema(description = "应用公钥")
    private String appPublicKey;

    @NotBlank(message = "notifyUrl 不能为空")
    @Schema(description = "服务器异步通知路径")
    private String notifyUrl;

    @NotBlank(message = "returnUrl 不能为空")
    @Schema(description = "页面跳转同步通知页面路径")
    private String returnUrl;

    @NotBlank(message = "signType 不能为空")
    @Schema(description = "签名方式")
    private String signType;

    @NotBlank(message = "charset 不能为空")
    @Schema(description = "字符编码格式")
    private String charset;

    @Schema(description = "是否生产环境")
    private Integer isProd;

    @Schema(description = "排序")
    private Integer sort;

    @Schema(description = "备注")
    private String remark;
}
