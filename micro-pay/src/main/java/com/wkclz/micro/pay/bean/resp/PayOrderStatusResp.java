package com.wkclz.micro.pay.bean.resp;

import com.wkclz.web.bean.EntityResp;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.util.Date;

@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "支付订单状态响应")
public class PayOrderStatusResp extends EntityResp {

    @Schema(description = "订单号")
    private String orderNo;

    @Schema(description = "支付订单号")
    private String outTradeNo;

    @Schema(description = "支付状态")
    private String payStatus;

    @Schema(description = "支付方式")
    private String payMethod;

    @Schema(description = "支付流水号")
    private String payFlowNo;

    @Schema(description = "支付时间")
    private Date payTime;

    @Schema(description = "平台总金额")
    private BigDecimal totalAmount;

    @Schema(description = "平台支付金额")
    private BigDecimal paymentAmount;
}
