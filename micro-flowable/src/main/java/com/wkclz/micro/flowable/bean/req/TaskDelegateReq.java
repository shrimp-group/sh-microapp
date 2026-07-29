package com.wkclz.micro.flowable.bean.req;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "任务委派请求")
public class TaskDelegateReq {
    @NotBlank(message = "taskId 不能为空")
    @Schema(description = "任务 ID")
    private String taskId;
    @NotBlank(message = "目标用户不能为空")
    @Schema(description = "目标用户 ID")
    private String targetUserId;
    @Schema(description = "委派说明")
    private String comment;
}
