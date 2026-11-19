package com.wkclz.micro.dag.bean.enums;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ExecutionEventTest {

    @Test
    void nodeLog_desc() {
        assertEquals("过程日志", ExecutionEvent.NODE_LOG.getDesc());
    }
}
