package com.wkclz.micro.dag.bean.entity;

import com.wkclz.core.base.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * DAG 执行记录
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class DagExecutionLog extends BaseEntity {

    @Schema(description = "实例流水号")
    private String instanceCode;

    @Schema(description = "节点编码")
    private String nodeCode;

    @Schema(description = "第几次执行")
    private Integer attempt;

    @Schema(description = "CLAIM/SUCCESS/FAIL/TIMEOUT/INTERRUPT/RETRY_SCHEDULED/LEASE_RECLAIM/SKIP/RESET/CLEANUP")
    private String event;

    @Schema(description = "详情(异常栈摘要/参数快照)")
    private String detailJson;

    @Schema(description = "执行机器标识")
    private String worker;
}
