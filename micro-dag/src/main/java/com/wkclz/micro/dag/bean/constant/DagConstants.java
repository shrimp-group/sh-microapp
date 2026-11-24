package com.wkclz.micro.dag.bean.constant;

/**
 * DAG 模块常量（bean 包内散落魔法值的集中处，按业务语义分节）
 */
public final class DagConstants {

    private DagConstants() {
    }

    // ==================== 边类型（dag_edge.type / EdgeDef.type） ====================

    /** 普通依赖边 */
    public static final String EDGE_TYPE_NORMAL = "NORMAL";

    /** 条件分支边（whenExpr 非空） */
    public static final String EDGE_TYPE_CONDITION = "CONDITION";

    // ==================== 调度通道（dag.channel：db=仅轮询 / pubsub=仅事件唤醒） ====================

    /** 仅轮询通道 */
    public static final String CHANNEL_DB = "db";

    /** 仅事件唤醒通道 */
    public static final String CHANNEL_PUBSUB = "pubsub";

    // ==================== 租约与时间换算 ====================

    /** 租约时长在节点 timeoutMs 基础上追加的余量 ms（保证租约恒覆盖超时窗口） */
    public static final long LEASE_MARGIN_MS = 30_000L;

    /** 毫秒 → 纳秒换算系数 */
    public static final long MS_TO_NS = 1_000_000L;
}
