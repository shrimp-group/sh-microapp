package com.wkclz.micro.dag.bean.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/** 实例五态 */
@Getter
@RequiredArgsConstructor
public enum InstanceStatus {

    RUNNING("运行中"),
    SUCCESS("成功"),
    FAILED("失败"),
    STOPPED("已停止"),
    TERMINATED("已终止"),
    ;

    /** 中文含义 */
    private final String desc;

    /** 实例是否处于可发起重跑/改参的稳定态（非运行中） */
    public boolean isStable() {
        return this != RUNNING;
    }
}
