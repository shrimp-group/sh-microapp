package com.wkclz.micro.dag.bean.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/** NodeHandler 分类：系统节点永久生效，demo 节点受 dag.demo-enabled 开关限制，业务节点由接入方注册 */
@Getter
@RequiredArgsConstructor
public enum NodeCategory {

    SYSTEM("系统节点"),
    DEMO("演示节点"),
    BUSINESS("业务节点"),
    ;

    /** 中文含义 */
    private final String desc;
}
