package com.wkclz.micro.points.bean.req;

import com.wkclz.micro.points.bean.enums.PointsSourceType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 积分发放入参
 * 供业务方/管理员调用发放服务使用，sourceNo 作为幂等键
 */
@Data
@Schema(description = "积分发放入参")
public class PointsIssueReq {

    @Schema(description = "租户编码")
    private String tenantCode;

    @NotBlank(message = "userCode 不能为空")
    @Schema(description = "用户编码", requiredMode = Schema.RequiredMode.REQUIRED)
    private String userCode;

    @NotNull(message = "points 不能为空")
    @Schema(description = "发放积分数", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer points;

    @Schema(description = "发放原因")
    private String reason;

    @Schema(description = "到期时间，为空时取 DB 默认 2099-12-31 23:59:59")
    private LocalDateTime expireTime;

    @NotBlank(message = "sourceNo 不能为空")
    @Schema(description = "来源单据号（业务单据号，作为幂等键）", requiredMode = Schema.RequiredMode.REQUIRED)
    private String sourceNo;

    @Schema(description = "积分来源类型，默认 ISSUANCE；管理员手动发放时由 REST 层强制设为 ADMIN_ISSUE")
    private String pointSourceType = PointsSourceType.ISSUANCE.name();

}
