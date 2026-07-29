package com.wkclz.micro.flowable.bean.resp;

import com.wkclz.web.bean.EntityResp;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "审批意见响应")
public class ApprovalResp extends EntityResp {
    @Schema(description = "申请单 ID")
    private Long applyId;
    @Schema(description = "流程实例 ID")
    private String procInsId;
    @Schema(description = "任务 ID")
    private String taskId;
    @Schema(description = "节点 key")
    private String nodeKey;
    @Schema(description = "节点名称")
    private String nodeName;
    @Schema(description = "审批人")
    private String approverId;
    @Schema(description = "审批动作")
    private String action;
    @Schema(description = "审批意见")
    private String comment;
    @Schema(description = "目标用户")
    private String targetUserId;
}
