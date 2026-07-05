package com.wkclz.micro.pay.bean.req;


import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.io.Serializable;

@Data
@Schema(description = "发起支付请求")
public class PayOrderReq implements Serializable {

    @NotBlank(message = "orderNo 不能为空")
    @Schema(description = "订单号")
    private String orderNo;


    @NotBlank(message = "payMethod 不能为空")
    @Schema(description = "支付方式")
    private String payMethod;


    private String terminalType;



}
