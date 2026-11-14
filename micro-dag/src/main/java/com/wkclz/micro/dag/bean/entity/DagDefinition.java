package com.wkclz.micro.dag.bean.entity;

import com.wkclz.core.base.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * DAG 流程定义
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class DagDefinition extends BaseEntity {

    @Schema(description = "流程编码")
    private String dagCode;

    @Schema(description = "流程名称")
    private String dagName;

    @Schema(description = "定义版本，发布递增")
    private Integer dagVersion;

    @Schema(description = "状态：DRAFT/PUBLISHED/OFFLINE")
    private String status;
}
