package com.wkclz.micro.dag.bean.entity;

import com.wkclz.core.base.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * DAG 定义节点
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class DagNode extends BaseEntity {

    @Schema(description = "流程编码")
    private String dagCode;

    @Schema(description = "定义版本")
    private Integer dagVersion;

    @Schema(description = "节点编码")
    private String nodeCode;

    @Schema(description = "节点名称")
    private String nodeName;

    @Schema(description = "NodeHandler 编码")
    private String handler;

    @Schema(description = "节点超时 ms，0=不限")
    private Long timeoutMs;

    @Schema(description = "最大重试次数")
    private Integer maxRetry;

    @Schema(description = "重试间隔秒")
    private Integer intervalSec;

    @Schema(description = "退避策略 FIXED/EXPONENTIAL")
    private String backoff;

    @Schema(description = "节点参数 JSON")
    private String paramsJson;

    @Schema(description = "画布坐标 X")
    private Integer posX;

    @Schema(description = "画布坐标 Y")
    private Integer posY;
}
