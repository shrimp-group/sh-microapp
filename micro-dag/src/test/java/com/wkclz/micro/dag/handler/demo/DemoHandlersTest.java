package com.wkclz.micro.dag.handler.demo;

import com.wkclz.core.exception.ValidationException;
import com.wkclz.micro.dag.api.NodeContext;
import com.wkclz.micro.dag.api.NodeResult;
import com.wkclz.micro.dag.bean.enums.NodeCategory;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * demo handler 单元测试
 */
class DemoHandlersTest {

    private NodeContext ctx(String inputJson, AtomicBoolean interrupted) {
        return new NodeContext("INS-1", "node-1", "TRC-1", 1, inputJson, interrupted == null ? () -> false : interrupted::get);
    }

    // ---------- category ----------

    @Test
    void demoHandlersCategoryIsDemo() {
        assertEquals(NodeCategory.DEMO, new LogPrintHandler().category());
        assertEquals(NodeCategory.DEMO, new DelayHandler().category());
        assertEquals(NodeCategory.DEMO, new TransformHandler().category());
        assertEquals(NodeCategory.DEMO, new HttpCallHandler().category());
        assertEquals(NodeCategory.DEMO, new ScriptExecHandler().category());
    }

    // ---------- demo-log-print ----------

    @Test
    void logPrintEchoesParams() {
        LogPrintHandler handler = new LogPrintHandler();
        assertEquals("demo-log-print", handler.nodeCode());
        NodeResult result = handler.execute(ctx("{\"msg\":\"hello\"}", null));
        assertEquals(Map.of("echo", Map.of("msg", "hello")), result.getOutput());
    }

    @Test
    void logPrintBlankParamsReturnsEmptyEcho() {
        LogPrintHandler handler = new LogPrintHandler();
        NodeResult result = handler.execute(ctx(null, null));
        assertEquals(Map.of("echo", Map.of()), result.getOutput());
    }

    // ---------- demo-delay ----------

    @Test
    void delaySleepsConfiguredMs() throws Exception {
        DelayHandler handler = new DelayHandler();
        NodeResult result = handler.execute(ctx("{\"ms\":250}", null));
        assertNotNull(result.getOutput());
        assertTrue((Long) result.getOutput().get("sleptMs") >= 250);
    }

    @Test
    void delayDefaultsTo1000MsCap300000() {
        DelayHandler handler = new DelayHandler();
        assertEquals(1000L, handler.resolveMs(ctx(null, null)));
        assertEquals(300000L, handler.resolveMs(ctx("{\"ms\":999999}", null)));
        assertThrows(ValidationException.class, () -> handler.resolveMs(ctx("{\"ms\":\"abc\"}", null)));
    }

    @Test
    void delayCooperativeInterrupt() throws Exception {
        DelayHandler handler = new DelayHandler();
        AtomicBoolean flag = new AtomicBoolean(true);
        assertThrows(InterruptedException.class, () -> handler.execute(ctx("{\"ms\":1000}", flag)));
    }

    // ---------- demo-transform ----------

    @Test
    void transformMergesParamsAndProcessedAt() {
        TransformHandler handler = new TransformHandler();
        NodeResult result = handler.execute(ctx("{\"a\":1,\"b\":\"x\"}", null));
        Map<String, Object> output = result.getOutput();
        assertEquals(1, output.get("a"));
        assertEquals("x", output.get("b"));
        assertNotNull(output.get("processedAt"));
    }

    // ---------- demo-http-call ----------

    @Test
    void httpMissingUrlRejected() {
        HttpCallHandler handler = new HttpCallHandler();
        assertThrows(ValidationException.class, () -> handler.execute(ctx(null, null)));
    }

    @Test
    void httpUnsupportedMethodRejected() {
        HttpCallHandler handler = new HttpCallHandler();
        assertThrows(ValidationException.class, () -> handler.execute(ctx("{\"url\":\"http://localhost:1/x\",\"method\":\"BREW\"}", null)));
    }

    // ---------- demo-script-exec ----------

    @Test
    void scriptExecutesHandleFunction() {
        ScriptExecHandler handler = new ScriptExecHandler();
        String script = "function handle(input) { return { upper: input.msg.toUpperCase(), n: input.n * 2 }; }";
        NodeResult result = handler.execute(ctx("{\"script\":\"" + script.replace("\"", "\\\"") + "\",\"msg\":\"hi\",\"n\":21}", null));
        assertEquals("HI", result.getOutput().get("upper"));
        assertEquals(42, result.getOutput().get("n"));
    }

    @Test
    void scriptScalarResultWrapped() {
        ScriptExecHandler handler = new ScriptExecHandler();
        String script = "function handle(input) { return 7; }";
        NodeResult result = handler.execute(ctx("{\"script\":\"" + script + "\"}", null));
        assertEquals(Map.of("result", 7), result.getOutput());
    }

    @Test
    void scriptMissingOrNoHandleRejected() {
        ScriptExecHandler handler = new ScriptExecHandler();
        assertThrows(ValidationException.class, () -> handler.execute(ctx(null, null)));
        assertThrows(ValidationException.class, () -> handler.execute(ctx("{\"script\":\"var x = 1;\"}", null)));
    }
}
