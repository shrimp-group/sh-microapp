package com.wkclz.micro.points.bean.resp;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 积分发放出参
 */
@Data
@Schema(description = "积分发放出参")
public class PointsIssueResp {

    @Schema(description = "本次发放流水号")
    private String flowNo;

    @Schema(description = "本次发放积分数")
    private BigDecimal points;

    @Schema(description = "钱包可用积分余额")
    private BigDecimal availablePoints;

    @Schema(description = "钱包历史总获得积分")
    private BigDecimal totalEarnedPoints;

}
