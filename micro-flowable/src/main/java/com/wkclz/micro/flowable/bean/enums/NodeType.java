package com.wkclz.micro.flowable.bean.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/** 流程节点类型 */
@Getter
@RequiredArgsConstructor
public enum NodeType {

    START("开始"),
    APPROVAL("审批"),
    CC("抄送"),
    GATEWAY("网关"),
    END("结束"),
    ;

    /** 中文含义 */
    private final String desc;
}
