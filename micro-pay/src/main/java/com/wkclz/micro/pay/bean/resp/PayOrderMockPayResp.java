package com.wkclz.micro.pay.bean.resp;

import com.wkclz.web.bean.EntityResp;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "模拟支付响应")
public class PayOrderMockPayResp extends EntityResp {

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
    private LocalDateTime payTime;
}
