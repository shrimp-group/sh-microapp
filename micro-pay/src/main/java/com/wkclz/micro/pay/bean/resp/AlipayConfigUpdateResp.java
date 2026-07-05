package com.wkclz.micro.pay.bean.resp;

import com.wkclz.web.bean.EntityResp;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "支付宝配置更新响应")
public class AlipayConfigUpdateResp extends EntityResp {

    @Schema(description = "租户编码")
    private String tenantCode;

    @Schema(description = "应用ID")
    private String appId;

    @Schema(description = "商户私钥")
    private String merchantPrivateKey;

    @Schema(description = "支付宝公钥")
    private String alipayPublicKey;

    @Schema(description = "应用公钥")
    private String appPublicKey;

    @Schema(description = "服务器异步通知路径")
    private String notifyUrl;

    @Schema(description = "页面跳转同步通知页面路径")
    private String returnUrl;

    @Schema(description = "签名方式")
    private String signType;

    @Schema(description = "字符编码格式")
    private String charset;

    @Schema(description = "是否生产环境")
    private Integer isProd;
}
