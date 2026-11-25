package com.wkclz.micro.flowable.bean.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/** 流程设计状态 */
@Getter
@RequiredArgsConstructor
public enum DesignStatus {

    DRAFT("草稿"),
    DEPLOYED("已部署"),
    DISABLED("已停用"),
    ;

    /** 中文含义 */
    private final String desc;
}
