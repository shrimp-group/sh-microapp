package com.wkclz.micro.pay.bean.req;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Schema(description = "支付订单创建请求")
public class PayOrderCreateReq {

    @NotBlank(message = "userCode 不能为空")
    @Schema(description = "用户编码", requiredMode = Schema.RequiredMode.REQUIRED)
    private String userCode;

    @NotBlank(message = "orderNo 不能为空")
    @Schema(description = "订单号", requiredMode = Schema.RequiredMode.REQUIRED)
    private String orderNo;

    @NotBlank(message = "payMethod 不能为空")
    @Schema(description = "支付方式", requiredMode = Schema.RequiredMode.REQUIRED)
    private String payMethod;

    @NotBlank(message = "terminalType 不能为空")
    @Schema(description = "终端类型", requiredMode = Schema.RequiredMode.REQUIRED)
    private String terminalType;

    @NotNull(message = "totalAmount 不能为空")
    @Schema(description = "总金额", requiredMode = Schema.RequiredMode.REQUIRED)
    private BigDecimal totalAmount;

    @NotBlank(message = "body 不能为空")
    @Schema(description = "商品描述", requiredMode = Schema.RequiredMode.REQUIRED)
    private String body;

    @NotBlank(message = "detail 不能为空")
    @Schema(description = "商品详情", requiredMode = Schema.RequiredMode.REQUIRED)
    private String detail;

    @Schema(description = "优惠金额")
    private BigDecimal discountAmount;

    @Schema(description = "支付金额")
    private BigDecimal paymentAmount;
}
