package com.wkclz.micro.dag.bean.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/** 节点七态 */
@Getter
@RequiredArgsConstructor
public enum NodeStatus {

    PENDING("待执行"),
    RUNNING("执行中"),
    SUCCESS("成功"),
    FAILED("失败"),
    RETRYING("重试中"),
    SKIPPED("跳过"),
    MANUAL_STOPPED("人工停止"),
    ;

    /** 中文含义 */
    private final String desc;

    /** 是否终态（不再被调度） */
    public boolean isTerminal() {
        return this == SUCCESS || this == FAILED || this == SKIPPED || this == MANUAL_STOPPED;
    }

    /** 是否视为满足下游依赖（SKIPPED 视为满足） */
    public boolean satisfiesDownstream() {
        return this == SUCCESS || this == SKIPPED;
    }
}
