package com.wkclz.micro.dag.bean.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/** 执行记录事件 */
@Getter
@RequiredArgsConstructor
public enum ExecutionEvent {

    CLAIM("领取"),
    SUCCESS("成功"),
    FAIL("失败"),
    NOT_READY("未就绪"),
    TIMEOUT("超时"),
    INTERRUPT("中断"),
    RETRY_SCHEDULED("重试已排期"),
    LEASE_RECLAIM("租约回收"),
    SKIP("跳过"),
    RESET("重置"),
    CLEANUP("清理"),
    NODE_LOG("过程日志"),
    ;

    /** 中文含义 */
    private final String desc;
}
