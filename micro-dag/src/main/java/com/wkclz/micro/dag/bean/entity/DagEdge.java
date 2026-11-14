package com.wkclz.micro.dag.bean.entity;

import com.wkclz.core.base.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * DAG 定义边
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class DagEdge extends BaseEntity {

    @Schema(description = "流程编码")
    private String dagCode;

    @Schema(description = "定义版本")
    private Integer dagVersion;

    @Schema(description = "边编码")
    private String edgeCode;

    @Schema(description = "起始节点编码")
    private String fromCode;

    @Schema(description = "目标节点编码")
    private String toCode;

    @Schema(description = "类型：NORMAL/CONDITION")
    private String type;

    @Schema(description = "条件表达式(type=CONDITION 时非空)")
    private String whenExpr;
}
