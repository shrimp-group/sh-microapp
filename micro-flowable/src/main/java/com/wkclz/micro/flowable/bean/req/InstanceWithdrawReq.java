package com.wkclz.micro.flowable.bean.req;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "流程撤回请求")
public class InstanceWithdrawReq {
    @NotBlank(message = "流程实例 ID 不能为空")
    @Schema(description = "流程实例 ID")
    private String procInsId;
    @Schema(description = "撤回原因")
    private String comment;
}
