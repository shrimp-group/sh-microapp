package com.wkclz.micro.points.bean.req;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 积分试算入参
 * 试算可抵扣金额，只读，不修改任何数据
 */
@Data
@Schema(description = "积分试算入参")
public class PointsTrialReq {

    @Schema(description = "租户编码")
    private String tenantCode;

    @NotBlank(message = "userCode 不能为空")
    @Schema(description = "用户编码", requiredMode = Schema.RequiredMode.REQUIRED)
    private String userCode;

    @NotNull(message = "paymentAmount 不能为空")
    @Schema(description = "需付款现金金额（按 100:1 换算，100 积分 = 1 元）", requiredMode = Schema.RequiredMode.REQUIRED)
    private BigDecimal paymentAmount;

}
