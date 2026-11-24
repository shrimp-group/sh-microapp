package com.wkclz.micro.dag.bean.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/** 依赖边：from → to（from 为上游） */
@Schema(description = "依赖边定义：from → to（from 为上游）")
@Data
public class EdgeDef {

    /** 边编码 */
    @Schema(description = "边编码")
    private String edgeCode;
    /** 起始节点编码 */
    @Schema(description = "起始节点编码")
    private String fromCode;
    /** 目标节点编码 */
    @Schema(description = "目标节点编码")
    private String toCode;
    /** NORMAL / CONDITION，缺省 NORMAL */
    @Schema(description = "边类型 NORMAL/CONDITION，缺省 NORMAL")
    private String type;
    /** CONDITION 边的分支表达式，如 ${ctx.sourceType} == 'api' */
    @Schema(description = "分支表达式(type=CONDITION 时非空)")
    private String whenExpr;
}
