package com.wkclz.micro.flowable.bean.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/** 异常处理状态 */
@Getter
@RequiredArgsConstructor
public enum HandleStatus {

    PENDING("待处理"),
    RESOLVED("已解决"),
    IGNORED("已忽略"),
    ;

    /** 中文含义 */
    private final String desc;
}
