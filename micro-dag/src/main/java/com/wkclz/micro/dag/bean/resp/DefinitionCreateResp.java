package com.wkclz.micro.dag.bean.resp;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class DefinitionCreateResp {

    @Schema(description = "定义 id")
    private Long id;

    @Schema(description = "流程编码（后端生成，前缀 dag_）")
    private String dagCode;
}
