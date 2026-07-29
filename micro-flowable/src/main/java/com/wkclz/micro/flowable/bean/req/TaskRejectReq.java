package com.wkclz.micro.flowable.bean.req;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "任务驳回请求")
public class TaskRejectReq {
    @NotBlank(message = "taskId 不能为空")
    @Schema(description = "任务 ID")
    private String taskId;
    @Schema(description = "驳回意见")
    private String comment;
    @Schema(description = "目标节点 key（默认回上一节点）")
    private String targetNodeKey;
}
