package com.wkclz.micro.flowable.bean.resp;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "部署响应")
public class DesignDeployResp {
    @Schema(description = "部署 ID")
    private String deployId;
    @Schema(description = "流程定义 ID")
    private String procDefId;
}
