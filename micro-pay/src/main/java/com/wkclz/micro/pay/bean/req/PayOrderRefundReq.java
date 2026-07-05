package com.wkclz.micro.pay.bean.req;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Schema(description = "支付订单退款请求")
public class PayOrderRefundReq {

    @NotBlank(message = "orderNo 不能为空")
    @Schema(description = "订单号")
    private String orderNo;

    @Schema(description = "退款原因")
    private String reason;

    @Schema(description = "本次退款金额，null 表示全单退款")
    private BigDecimal refundAmount;

    @Schema(description = "本次退款单号，部分退款时必传")
    private String refundNo;

    @Schema(description = "子单号（订单详情行项目号）。为空 → 总单退款；非空 → 子单退款")
    private String subOrderNo;
}
