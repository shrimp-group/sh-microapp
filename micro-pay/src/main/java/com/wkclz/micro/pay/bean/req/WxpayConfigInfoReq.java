package com.wkclz.micro.pay.bean.req;

import com.wkclz.web.bean.IdReq;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "微信支付配置详情查询请求")
public class WxpayConfigInfoReq extends IdReq {
}
