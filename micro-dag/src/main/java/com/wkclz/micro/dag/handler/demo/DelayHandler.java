package com.wkclz.micro.dag.handler.demo;

import com.wkclz.core.exception.ValidationException;
import com.wkclz.micro.dag.api.NodeContext;
import com.wkclz.micro.dag.api.NodeHandler;
import com.wkclz.micro.dag.api.NodeResult;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

/**
 * demo-延时：休眠指定毫秒，演示协作式中断检查点（周期性 checkInterrupted）
 * 入参：ms（可选，默认 1000，上限 300000）
 */
@Slf4j
public class DelayHandler implements NodeHandler {

    private static final long DEFAULT_MS = 1000;
    private static final long MAX_MS = 300000;
    private static final long STEP_MS = 200;

    @Override
    public String nodeCode() {
        return "demo-delay";
    }

    @Override
    public String nodeName() {
        return "演示-延时";
    }

    @Override
    public NodeResult execute(NodeContext ctx) throws Exception {
        long ms = resolveMs(ctx);
        log.info("demo-delay: instance={} node={} ms={}", ctx.getInstanceCode(), ctx.getNodeCode(), ms);
        long slept = 0;
        while (slept < ms) {
            long step = Math.min(STEP_MS, ms - slept);
            Thread.sleep(step);
            slept += step;
            ctx.checkInterrupted();
        }
        return NodeResult.ok(Map.of("sleptMs", slept));
    }

    /** 解析 ms 参数：缺省 1000，上限 300000，非法值抛校验异常 */
    long resolveMs(NodeContext ctx) {
        Object v = ctx.inputParams().get("ms");
        if (v == null) {
            return DEFAULT_MS;
        }
        long ms;
        try {
            ms = v instanceof Number n ? n.longValue() : Long.parseLong(v.toString().trim());
        } catch (NumberFormatException e) {
            log.error("demo-delay: ms 参数非法 value={}", v, e);
            throw ValidationException.of("demo-delay ms 参数非法: " + v);
        }
        return Math.min(Math.max(ms, 0), MAX_MS);
    }
}
