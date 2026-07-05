package com.wkclz.micro.pay.bean.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 订单支付结果 DTO，承载支付成功后需回写订单的支付结果信息
 */
@Data
@Schema(description = "订单支付结果")
public class OrderPayResult implements Serializable {

    @Schema(description = "订单号")
    private String orderNo;

    @Schema(description = "支付方式")
    private String payMethod;

    @Schema(description = "支付流水号（第三方交易号）")
    private String payFlowNo;

    @Schema(description = "支付时间")
    private LocalDateTime payTime;
}
