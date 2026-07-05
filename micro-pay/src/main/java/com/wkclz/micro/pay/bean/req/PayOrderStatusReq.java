package com.wkclz.micro.pay.bean.req;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "支付订单状态查询请求")
public class PayOrderStatusReq {

    @NotBlank(message = "orderNo 不能为空")
    @Schema(description = "订单号")
    private String orderNo;
}
