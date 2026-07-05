package com.wkclz.micro.points.bean.req;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 对账查询入参
 * 核对消费流水与扣减记录一致性
 */
@Data
@Schema(description = "对账查询入参")
public class PointsReconcileReq {

    @Schema(description = "租户编码")
    private String tenantCode;

    @Schema(description = "用户编码")
    private String userCode;

    @Schema(description = "查询起始时间（可选）")
    private LocalDateTime startTime;

    @Schema(description = "查询结束时间（可选）")
    private LocalDateTime endTime;

}
