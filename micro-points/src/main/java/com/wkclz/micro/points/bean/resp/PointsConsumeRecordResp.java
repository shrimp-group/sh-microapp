package com.wkclz.micro.points.bean.resp;

import com.wkclz.web.bean.EntityResp;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 消费流水出参
 * 字段同 PointsConsumeRecord，含基础字段
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "消费流水出参")
public class PointsConsumeRecordResp extends EntityResp {

    @Schema(description = "租户编码")
    private String tenantCode;

    @Schema(description = "用户编码")
    private String userCode;

    @Schema(description = "流水号（系统生成，唯一标识）")
    private String flowNo;

    @Schema(description = "使用时间")
    private LocalDateTime consumeTime;

    @Schema(description = "使用积分数")
    private Integer points;

    @Schema(description = "使用原因")
    private String reason;

    @Schema(description = "关联单据号（业务单据，唯一）")
    private String orderNo;

    @Schema(description = "状态（FROZEN 冻结 / DEDUCTED 已扣减）")
    private String status;

}
