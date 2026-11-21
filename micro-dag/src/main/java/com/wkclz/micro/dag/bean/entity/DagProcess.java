package com.wkclz.micro.dag.bean.entity;

import com.wkclz.core.base.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * DAG 流程（一条流程一行，名称唯一持有处）
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class DagProcess extends BaseEntity {

    @Schema(description = "流程编码")
    private String dagCode;

    @Schema(description = "流程名称")
    private String dagName;
}
