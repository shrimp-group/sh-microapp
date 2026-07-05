package com.wkclz.micro.points.bean.req;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 积分消费入参
 * 两阶段消费之第一阶段：校验余额 + 冻结 + 触发异步扣减，orderNo 作为幂等键
 */
@Data
@Schema(description = "积分消费入参")
public class PointsConsumeReq {

    @Schema(description = "租户编码")
    private String tenantCode;

    @NotBlank(message = "userCode 不能为空")
    @Schema(description = "用户编码", requiredMode = Schema.RequiredMode.REQUIRED)
    private String userCode;

    @NotNull(message = "points 不能为空")
    @Schema(description = "消费积分数", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer points;

    @Schema(description = "消费原因")
    private String reason;

    @NotBlank(message = "orderNo 不能为空")
    @Schema(description = "关联单据号（业务单据，唯一，作为幂等键）", requiredMode = Schema.RequiredMode.REQUIRED)
    private String orderNo;

}
