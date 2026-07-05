package com.wkclz.micro.points.bean.req;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 积分回退入参
 * 以发放方式回退积分。
 * <p>
 * 幂等键：
 * <ul>
 *   <li>refundNo 非空时，幂等键为 REFUND:refundNo（支持同一 orderNo 多次部分退款）</li>
 *   <li>refundNo 为空时，幂等键为 REFUND:orderNo（全额退款，向后兼容）</li>
 * </ul>
 * orderNo 始终用于查找原消费记录、超额防护计算、回退获取流水的 source_no。
 */
@Data
@Schema(description = "积分回退入参")
public class PointsRefundReq {

    @Schema(description = "租户编码")
    private String tenantCode;

    @NotBlank(message = "userCode 不能为空")
    @Schema(description = "用户编码", requiredMode = Schema.RequiredMode.REQUIRED)
    private String userCode;

    @NotNull(message = "points 不能为空")
    @Schema(description = "回退积分数", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer points;

    @Schema(description = "回退原因")
    private String reason;

    @NotBlank(message = "orderNo 不能为空")
    @Schema(description = "原消费单据号（用于查找原消费记录与超额防护，全额退款时也作为幂等键）", requiredMode = Schema.RequiredMode.REQUIRED)
    private String orderNo;

    @Schema(description = "退款单号（部分退款幂等标识，全额退款可不传）")
    private String refundNo;

    @Schema(description = "回退积分新到期时间，为空时取 DB 默认 2099-12-31 23:59:59")
    private LocalDateTime expireTime;

}
