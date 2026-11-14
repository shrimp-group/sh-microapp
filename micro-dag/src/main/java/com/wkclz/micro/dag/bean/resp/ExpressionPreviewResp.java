package com.wkclz.micro.dag.bean.resp;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class ExpressionPreviewResp {

    @Schema(description = "表达式是否可解析")
    private Boolean parseOk;

    @Schema(description = "求值结果（可解析时返回）")
    private Boolean result;

    @Schema(description = "解析错误信息")
    private String errorMsg;
}
