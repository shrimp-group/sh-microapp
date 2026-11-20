package com.wkclz.micro.dag.handler.demo;

import com.wkclz.core.exception.ValidationException;
import com.wkclz.micro.dag.api.NodeContext;
import com.wkclz.micro.dag.api.NodeResult;
import com.wkclz.micro.dag.bean.enums.NodeCategory;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * demo-fail handler 单元测试
 */
class DemoFailHandlerTest {

    private NodeContext ctx(String instanceCode, int attempt, String inputJson) {
        return new NodeContext(instanceCode, "demo-fail", null, attempt, inputJson, () -> false);
    }

    @Test
    void failWithinFailTimes() {
        DemoFailHandler handler = new DemoFailHandler();
        assertEquals("demo-fail", handler.nodeCode());
        assertEquals(NodeCategory.DEMO, handler.category());
        NodeResult r0 = handler.execute(ctx("it-1", 0, "{\"failTimes\":2,\"msg\":\"boom\"}"));
        assertFalse(r0.isSuccess());
        assertEquals(0, r0.getOutput().get("attempt"));
        assertEquals("boom", r0.getOutput().get("msg"));
        assertNull(r0.getTraceCode());
        NodeResult r1 = handler.execute(ctx("it-1", 1, "{\"failTimes\":2,\"msg\":\"boom\"}"));
        assertFalse(r1.isSuccess());
        assertEquals(1, r1.getOutput().get("attempt"));
        assertNull(r1.getTraceCode());
    }

    @Test
    void successAfterFailTimes() {
        DemoFailHandler handler = new DemoFailHandler();
        NodeResult result = handler.execute(ctx("it-1", 2, "{\"failTimes\":2,\"msg\":\"boom\"}"));
        assertTrue(result.isSuccess());
        assertEquals(2, result.getOutput().get("attempt"));
        assertEquals("demo-trace-it-1", result.getTraceCode());
    }

    @Test
    void emptyParamsSuccessDirectly() {
        DemoFailHandler handler = new DemoFailHandler();
        NodeResult result = handler.execute(ctx("it-1", 3, "{}"));
        assertTrue(result.isSuccess());
        assertEquals(3, result.getOutput().get("attempt"));
        assertEquals("success", result.getOutput().get("msg"));
        assertTrue(result.getTraceCode().startsWith("demo-trace-"));
    }

    @Test
    void illegalFailTimesRejected() {
        DemoFailHandler handler = new DemoFailHandler();
        assertThrows(ValidationException.class, () -> handler.execute(ctx("it-1", 0, "{\"failTimes\":\"abc\"}")));
    }
}
