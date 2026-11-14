package com.wkclz.micro.dag.cache;

/**
 * Redis Pub/Sub 频道常量（仅加速通道，DB 为事实源）
 */
public final class DagChannel {

    /** 中断广播，payload = instanceCode:nodeCode */
    public static final String INTERRUPT = "sh:micro:dag:interrupt";

    /** 调度唤醒广播，payload = 时间戳 */
    public static final String WAKEUP = "sh:micro:dag:wakeup";

    private DagChannel() {
    }
}
