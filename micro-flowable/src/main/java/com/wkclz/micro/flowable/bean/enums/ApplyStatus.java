package com.wkclz.micro.flowable.bean.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/** 申请单状态 */
@Getter
@RequiredArgsConstructor
public enum ApplyStatus {

    DRAFT("草稿"),
    RUNNING("运行中"),
    APPROVED("已通过"),
    REJECTED("已驳回"),
    TERMINATED("已终止"),
    WITHDRAWN("已撤回"),
    ;

    /** 中文含义 */
    private final String desc;
}
