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

    // ==================== 系统节点入参快照键（launch 时写入 input_params_json，最后合并，实例参数不可覆盖） ====================

    /** system-timer 定时器节点：直接上游节点编码快照（List&lt;String&gt;，入边 fromCode 去重） */
    public static final String SNAPSHOT_KEY_UPSTREAMS = "upstreams";

    // ==================== 租约与时间换算 ====================

    /** 租约时长在节点 timeoutMs 基础上追加的余量 ms（保证租约恒覆盖超时窗口） */
    public static final long LEASE_MARGIN_MS = 30_000L;

    /** 毫秒 → 纳秒换算系数 */
    public static final long MS_TO_NS = 1_000_000L;
}
