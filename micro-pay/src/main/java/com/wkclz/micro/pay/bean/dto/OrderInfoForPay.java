package com.wkclz.micro.pay.bean.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 订单支付信息 DTO，承载从订单模块获取的支付所需信息
 */
@Data
@Schema(description = "订单支付信息")
public class OrderInfoForPay implements Serializable {

    @Schema(description = "订单号")
    private String orderNo;

    @Schema(description = "用户编码")
    private String userCode;

    @Schema(description = "租户编码")
    private String tenantCode;

    @Schema(description = "总金额")
    private BigDecimal totalAmount;

    @Schema(description = "优惠金额")
    private BigDecimal discountAmount;

    @Schema(description = "支付金额")
    private BigDecimal paymentAmount;

    @Schema(description = "本次订单使用的积分数量")
    private Integer points;

    @Schema(description = "订单描述")
    private String orderDesc;

    @Schema(description = "订单状态")
    private String orderStatus;
}
