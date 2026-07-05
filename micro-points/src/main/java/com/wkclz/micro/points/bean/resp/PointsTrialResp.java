package com.wkclz.micro.points.bean.resp;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 积分试算出参
 * 按 100:1 换算（100 积分 = 1 元）
 */
@Data
@Schema(description = "积分试算出参")
public class PointsTrialResp {

    @Schema(description = "钱包可用积分")
    private Integer availablePoints;

    @Schema(description = "可抵扣现金金额")
    private BigDecimal deductAmount;

    @Schema(description = "所需积分数")
    private Integer requiredPoints;

}
