package com.wkclz.micro.dag.bean.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/** 版本状态 */
@Getter
@RequiredArgsConstructor
public enum ProcessStatus {

    DRAFT("草稿"),
    PUBLISHED("已发布"),
    ;

    /** 中文含义 */
    private final String desc;
}
