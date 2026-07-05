package com.wkclz.micro.pay.bean.resp;

import com.wkclz.web.bean.EntityResp;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Map;

/**
 * 发起支付响应 DTO
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "发起支付响应")
public class PayOrderPayResp extends EntityResp {

    @Schema(description = "订单号")
    private String orderNo;

    @Schema(description = "支付订单号")
    private String outTradeNo;

    @Schema(description = "支付状态")
    private String payStatus;

    @Schema(description = "支付方式")
    private String payMethod;

    @Schema(description = "支付宝支付表单HTML（支付宝时返回）")
    private String aliPayBody;

    @Schema(description = "预支付ID（微信时返回）")
    private String prepayId;

    @Schema(description = "JSAPI调起支付参数（微信时返回）")
    private Map<String, Object> jsapiResult;
}
