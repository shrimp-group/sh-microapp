package com.wkclz.micro.dag.bean.entity;

import com.wkclz.core.base.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * DAG 流程实例
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class DagInstance extends BaseEntity {

    @Schema(description = "实例流水号(模块内生成)")
    private String instanceCode;

    @Schema(description = "唯一跟踪编码(可运行期回填)")
    private String traceCode;

    @Schema(description = "流程编码")
    private String dagCode;

    @Schema(description = "发起时锁定的定义版本")
    private Integer dagVersion;

    @Schema(description = "状态：RUNNING/SUCCESS/FAILED/STOPPED/TERMINATED")
    private String status;

    @Schema(description = "流程上下文(节点间传递的小 JSON)")
    private String contextJson;

    @Schema(description = "发起参数")
    private String bizParamsJson;
}
