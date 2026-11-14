package com.wkclz.micro.dag.bean.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/** 重试退避策略 */
@Getter
@RequiredArgsConstructor
public enum BackoffType {

    FIXED("固定间隔"),
    EXPONENTIAL("指数退避"),
    ;

    /** 中文含义 */
    private final String desc;
}
