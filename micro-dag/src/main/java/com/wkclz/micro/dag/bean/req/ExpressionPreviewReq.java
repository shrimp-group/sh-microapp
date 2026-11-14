package com.wkclz.micro.dag.bean.req;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Map;

@Data
public class ExpressionPreviewReq {

    @Schema(description = "分支表达式")
    private String expression;

    @Schema(description = "样例流程上下文，对应 ${ctx.*}")
    private Map<String, Object> ctx;

    @Schema(description = "样例节点输出，对应 ${out.<nodeCode>.*}")
    private Map<String, Map<String, Object>> nodeOutputs;
}
