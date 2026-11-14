package com.wkclz.micro.dag.bean.entity;

import com.wkclz.core.base.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * DAG 节点实例
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class DagNodeInstance extends BaseEntity {

    @Schema(description = "所属实例流水号")
    private String instanceCode;

    @Schema(description = "节点编码")
    private String nodeCode;

    @Schema(description = "节点名称(定义快照)")
    private String nodeName;

    @Schema(description = "状态：PENDING/RUNNING/SUCCESS/FAILED/RETRYING/SKIPPED/MANUAL_STOPPED")
    private String status;

    @Schema(description = "已执行次数")
    private Integer attempt;

    @Schema(description = "NodeHandler 编码(定义快照)")
    private String handler;

    @Schema(description = "最大重试次数(定义快照,可改参覆盖)")
    private Integer maxRetry;

    @Schema(description = "重试间隔秒(定义快照)")
    private Integer intervalSec;

    @Schema(description = "退避策略 FIXED/EXPONENTIAL")
    private String backoff;

    @Schema(description = "节点超时 ms，0=不限(定义快照)")
    private Long timeoutMs;

    @Schema(description = "节点输入参数(人工改参写入处)")
    private String inputParamsJson;

    @Schema(description = "节点输出")
    private String outputJson;

    @Schema(description = "领取者 instance标识:ip:port")
    private String leaseOwner;

    @Schema(description = "租约到期时间(故障转移依据)")
    private LocalDateTime leaseExpireTime;

    @Schema(description = "中断标记(人工停止请求)")
    private Integer interruptFlag;

    @Schema(description = "重跑前需执行清理钩子")
    private Integer cleanupFlag;

    @Schema(description = "异常摘要")
    private String errorMsg;

    @Schema(description = "RETRYING 状态的提升时间")
    private LocalDateTime nextRetryTime;

    @Schema(description = "跳过原因")
    private String skipReason;

    @Schema(description = "人工停止原因")
    private String stopReason;
}
