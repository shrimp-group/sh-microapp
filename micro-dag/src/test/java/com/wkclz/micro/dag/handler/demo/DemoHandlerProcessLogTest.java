package com.wkclz.micro.dag.handler.demo;

import com.wkclz.micro.dag.api.NodeContext;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.function.BooleanSupplier;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * demo handler 过程日志上报验证：正常执行也必须产生 NODE_LOG 可看的过程日志（ctx.log）
 */
class DemoHandlerProcessLogTest {

    private static final String INSTANCE_CODE = "INST20261123000001";
    private static final String NODE_CODE = "N_demo";

    private NodeContext context(String inputJson) {
        BooleanSupplier notInterrupted = () -> false;
        return new NodeContext(INSTANCE_CODE, NODE_CODE, null, 1, inputJson, notInterrupted);
    }

    @Test
    void logPrint_execute_reportsProcessLog() {
        LogPrintHandler handler = new LogPrintHandler();
        NodeContext ctx = context("{\"k\":\"v\"}");

        handler.execute(ctx);

        String logText = ctx.drainLog();
        assertTrue(logText != null && logText.contains("echo"), "log-print 应上报回显过程日志");
    }

    @Test
    void transform_execute_reportsProcessLog() {
        TransformHandler handler = new TransformHandler();
        NodeContext ctx = context("{\"a\":1}");

        handler.execute(ctx);

        String logText = ctx.drainLog();
        assertTrue(logText != null && logText.contains("转换输出"), "transform 应上报转换输出过程日志");
    }

    @Test
    void delay_execute_reportsProcessLog() throws Exception {
        DelayHandler handler = new DelayHandler();
        NodeContext ctx = context("{\"ms\":0}");

        handler.execute(ctx);

        String logText = ctx.drainLog();
        assertTrue(logText != null && logText.contains("延时"), "delay 应上报延时时长过程日志");
    }

    @Test
    void httpCall_requestLineLoggedBeforeRequest() {
        // 请求前先落过程日志（请求行），即使连接失败过程日志也已缓冲
        HttpCallHandler handler = new HttpCallHandler();
        NodeContext ctx = context("{\"url\":\"http://127.0.0.1:1/\"}");

        assertThrows(RuntimeException.class, () -> handler.execute(ctx));

        String logText = ctx.drainLog();
        assertTrue(logText != null && logText.contains("GET http://127.0.0.1:1/"), "http-call 请求前应上报请求行过程日志");
    }

    @Test
    void scriptExec_execute_reportsProcessLog() {
        ScriptExecHandler handler = new ScriptExecHandler();
        String script = "{\"script\":\"function handle(input){ return {ok: true} }\"}";
        NodeContext ctx = context(script);

        handler.execute(ctx);

        String logText = ctx.drainLog();
        assertTrue(logText != null && logText.contains("脚本执行完成"), "script-exec 应上报执行完成过程日志");
    }

    @Test
    void context_drainLog_emptyReturnsNull() {
        // 对照：未上报时 drainLog 返回 null（不落库）
        NodeContext ctx = context(null);
        assertFalse(ctx.drainLog() != null);
    }
}
