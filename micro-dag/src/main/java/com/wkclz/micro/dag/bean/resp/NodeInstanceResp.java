package com.wkclz.micro.dag.bean.resp;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class NodeInstanceResp {

    @Schema(description = "节点实例 id")
    private Long id;

    @Schema(description = "所属实例流水号")
    private String instanceCode;

    @Schema(description = "节点编码")
    private String nodeCode;

    @Schema(description = "节点名称")
    private String nodeName;

    @Schema(description = "节点状态")
    private String status;

    @Schema(description = "已执行次数")
    private Integer attempt;

    @Schema(description = "handler 编码")
    private String handler;

    @Schema(description = "最大重试次数")
    private Integer maxRetry;

    @Schema(description = "重试间隔秒")
    private Integer intervalSec;

    @Schema(description = "退避策略")
    private String backoff;

    @Schema(description = "超时 ms")
    private Long timeoutMs;

    @Schema(description = "画布坐标 X（DAG 视图渲染用，来自定义快照）")
    private Integer posX;

    @Schema(description = "画布坐标 Y（DAG 视图渲染用，来自定义快照）")
    private Integer posY;

    @Schema(description = "节点参数 JSON")
    private String inputParamsJson;

    @Schema(description = "节点输出 JSON")
    private String outputJson;

    @Schema(description = "领取者")
    private String leaseOwner;

    @Schema(description = "异常摘要")
    private String errorMsg;

    @Schema(description = "下次重试时间")
    private LocalDateTime nextRetryTime;

    @Schema(description = "跳过原因")
    private String skipReason;

    @Schema(description = "停止原因")
    private String stopReason;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "修改时间")
    private LocalDateTime updateTime;
}
