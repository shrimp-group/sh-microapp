package com.wkclz.micro.dag.bean.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/** 定义状态 */
@Getter
@RequiredArgsConstructor
public enum DefinitionStatus {

    DRAFT("草稿"),
    PUBLISHED("已发布"),
    OFFLINE("已下线"),
    ;

    /** 中文含义 */
    private final String desc;
}
