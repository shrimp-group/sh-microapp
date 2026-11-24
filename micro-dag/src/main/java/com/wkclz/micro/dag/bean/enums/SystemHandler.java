package com.wkclz.micro.dag.bean.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/** 系统 handler 编码：永久注册不受 dag.demo-enabled 限制，与节点类型一一对应 */
@Getter
@RequiredArgsConstructor
public enum SystemHandler {

    START(NodeType.START, "system-start"),
    END(NodeType.END, "system-end"),
    BRANCH(NodeType.BRANCH, "system-branch"),
    TIMER(NodeType.NORMAL, "system-timer"),
    ;

    /** 限定可搭配的节点类型 */
    private final NodeType nodeType;
    private final String code;

    /** 按 handler 编码解析，空或未知返回 null */
    public static SystemHandler of(String code) {
        if (code == null) {
            return null;
        }
        for (SystemHandler h : values()) {
            if (h.code.equals(code)) {
                return h;
            }
        }
        return null;
    }
}
