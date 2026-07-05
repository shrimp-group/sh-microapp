package com.wkclz.micro.points.bean.resp;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 对账结果出参
 * 核对消费流水与扣减记录（COMPLETED 动作记录）一致性
 */
@Data
@Schema(description = "对账结果出参")
public class PointsReconcileResp {

    @Schema(description = "消费流水号")
    private String consumeFlowNo;

    @Schema(description = "消费积分")
    private Integer points;

    @Schema(description = "扣减记录之和（COMPLETED 动作记录）")
    private Integer deductedSum;

    @Schema(description = "差异（消费积分 - 扣减记录之和）")
    private Integer diff;

    @Schema(description = "对账状态（一致 / 不一致 / 异常）")
    private String status;

}
