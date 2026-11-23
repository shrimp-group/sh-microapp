package com.wkclz.micro.dag.bean.req;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class InstanceRerunFromReq {

    @Schema(description = "实例流水号")
    private String instanceCode;

    @Schema(description = "从指定节点重跑的节点编码")
    private String nodeCode;

    @Schema(description = "实例参数 JSON（可选，非空时同步更新 bizParamsJson 与 contextJson）")
    private String instanceParamsJson;
}
