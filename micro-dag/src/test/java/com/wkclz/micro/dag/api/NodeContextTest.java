package com.wkclz.micro.dag.api;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class NodeContextTest {

    private NodeContext ctx(int maxLogChars) {
        return new NodeContext("INS-1", "node-1", "TRC-1", 1, null, () -> false, maxLogChars);
    }

    @Test
    void drainLog_accumulatesLinesAndEmpties() {
        NodeContext ctx = ctx(100);
        ctx.log("step-1");
        ctx.log("step-2");

        assertEquals("step-1\nstep-2", ctx.drainLog());
        assertNull(ctx.drainLog()); // drain 后无内容
        assertEquals(12, ctx.logCharCount());
        assertFalse(ctx.isTruncated());
    }

    @Test
    void log_overflow_discardsAndMarksTruncated() {
        NodeContext ctx = ctx(10);
        ctx.log("12345");
        ctx.log("67890"); // 累计 10，未超
        ctx.log("abcde"); // 累计 15 > 10：整条丢弃并置 truncated
        ctx.log("fg");    // 截断后继续丢弃

        assertEquals("12345\n67890", ctx.drainLog());
        assertTrue(ctx.isTruncated());
        assertEquals(17, ctx.logCharCount());
    }

    @Test
    void log_nullOrEmpty_ignored() {
        NodeContext ctx = ctx(10);
        ctx.log(null);
        ctx.log("");

        assertNull(ctx.drainLog());
        assertFalse(ctx.isTruncated());
        assertEquals(0, ctx.logCharCount());
    }
}
