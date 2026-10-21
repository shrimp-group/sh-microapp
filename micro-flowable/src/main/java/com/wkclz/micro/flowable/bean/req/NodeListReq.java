package com.wkclz.micro.flowable.bean.req;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "节点配置列表查询")
public class NodeListReq {
    @NotNull(message = "designCode 不能为空")
    @Schema(description = "设计编码")
    private String designCode;
}
