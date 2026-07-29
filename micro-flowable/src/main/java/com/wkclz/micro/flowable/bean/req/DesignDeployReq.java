package com.wkclz.micro.flowable.bean.req;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "流程部署请求")
public class DesignDeployReq {
    @NotNull(message = "id 不能为空")
    @Schema(description = "设计 ID")
    private Long id;
}
