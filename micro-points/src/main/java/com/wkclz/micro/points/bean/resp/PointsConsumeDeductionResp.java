package com.wkclz.micro.points.bean.resp;

import com.wkclz.micro.points.bean.entity.PointsDeductionRecord;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 消费扣减明细出参
 * 用于对账页面展示，包含消费流水及其关联的扣减动作记录
 */
@Data
@Schema(description = "消费扣减明细出参")
public class PointsConsumeDeductionResp {

    @Schema(description = "消费流水号")
    private String consumeFlowNo;

    @Schema(description = "消费时间")
    private LocalDateTime consumeTime;

    @Schema(description = "消费积分")
    private Integer points;

    @Schema(description = "关联单据号")
    private String orderNo;

    @Schema(description = "消费状态（FROZEN 冻结 / DEDUCTED 已扣减）")
    private String status;

    @Schema(description = "扣减动作记录列表（COMPLETED 动作记录，earn_flow_no 非空）")
    private List<PointsDeductionRecord> deductions;

    @Schema(description = "已扣减总额（便于前端展示）")
    private Integer deductedSum;

}
