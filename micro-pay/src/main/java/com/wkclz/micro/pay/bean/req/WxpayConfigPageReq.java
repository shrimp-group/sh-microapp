package com.wkclz.micro.pay.bean.req;

import com.wkclz.web.bean.PageReq;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "微信支付配置分页查询请求")
public class WxpayConfigPageReq extends PageReq {

    @Schema(description = "AppId")
    private String appId;

    @Schema(description = "支付商户号")
    private String mchId;
}
