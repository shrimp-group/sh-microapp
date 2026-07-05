package com.wkclz.micro.points.bean.resp;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 积分消费出参
 * 两阶段消费之第一阶段结果，status=FROZEN，真实扣减异步完成
 */
@Data
@Schema(description = "积分消费出参")
public class PointsConsumeResp {

    @Schema(description = "本次消费流水号")
    private String flowNo;

    @Schema(description = "消费状态（FROZEN 冻结 / DEDUCTED 已扣减）")
    private String status;

    @Schema(description = "消费积分数")
    private Integer points;

}
