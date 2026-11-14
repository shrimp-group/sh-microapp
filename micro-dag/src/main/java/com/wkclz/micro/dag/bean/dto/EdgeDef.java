package com.wkclz.micro.dag.bean.dto;

import lombok.Data;

/** 依赖边：from → to（from 为上游） */
@Data
public class EdgeDef {

    public static final String TYPE_NORMAL = "NORMAL";
    public static final String TYPE_CONDITION = "CONDITION";

    /** 边编码 */
    private String edgeCode;
    /** 起始节点编码 */
    private String fromCode;
    /** 目标节点编码 */
    private String toCode;
    /** NORMAL / CONDITION，缺省 NORMAL */
    private String type;
    /** CONDITION 边的分支表达式，如 ${ctx.sourceType} == 'api' */
    private String whenExpr;
}
