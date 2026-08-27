package com.wkclz.micro.flowable.bean.entity;

import com.wkclz.core.base.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
@Schema(description = "审批意见")
public class FlowableApproval extends BaseEntity {

    @Schema(description = "关联 apply.id")
    private Long applyId;

    @Schema(description = "流程实例 ID")
    private String procInsId;

    @Schema(description = "flowable 任务 ID")
    private String taskId;

    @Schema(description = "节点 key")
    private String nodeKey;

    @Schema(description = "节点名称")
    private String nodeName;

    @Schema(description = "审批人用户 ID")
    private String approverId;

    @Schema(description = "审批动作")
    private String action;

    @Schema(description = "审批意见")
    private String comment;

    @Schema(description = "目标用户")
    private String targetUserId;

    @Schema(description = "租户编码")
    private String tenantCode;
}
