package com.wkclz.micro.points.bean.resp;

import com.wkclz.web.bean.EntityResp;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 获取流水出参
 * 字段同 PointsEarnRecord，含基础字段
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "获取流水出参")
public class PointsEarnRecordResp extends EntityResp {

    @Schema(description = "租户编码")
    private String tenantCode;

    @Schema(description = "用户编码")
    private String userCode;

    @Schema(description = "流水号（系统生成，唯一标识）")
    private String flowNo;

    @Schema(description = "获取时间")
    private LocalDateTime earnTime;

    @Schema(description = "获取积分数")
    private Integer points;

    @Schema(description = "获取原因")
    private String reason;

    @Schema(description = "到期时间（DB 默认 2099-12-31 23:59:59）")
    private LocalDateTime expireTime;

    @Schema(description = "已使用积分数")
    private Integer usedPoints;

    @Schema(description = "可用积分数")
    private Integer availablePoints;

    @Schema(description = "是否已使用完(0/1)")
    private Integer isUsedUp;

    @Schema(description = "积分来源类型（ISSUANCE 发放 / REFUND 回退 / ADMIN_ISSUE 管理员手动发放）")
    private String pointSourceType;

    @Schema(description = "来源单据号（发放时为业务单据号；回退时为原消费单据号 order_no）")
    private String sourceNo;

}
