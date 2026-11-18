package com.wkclz.micro.dag.handler.demo;

import com.wkclz.micro.dag.api.NodeContext;
import com.wkclz.micro.dag.api.NodeHandler;
import com.wkclz.micro.dag.api.NodeResult;
import com.wkclz.micro.dag.bean.enums.NodeCategory;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

/**
 * demo-日志打印：打印节点入参并原样回显，演示最小 handler 实现
 */
@Slf4j
public class LogPrintHandler implements NodeHandler {

    @Override
    public String nodeCode() {
        return "demo-log-print";
    }

    @Override
    public String nodeName() {
        return "演示-日志打印";
    }

    @Override
    public NodeCategory category() {
        return NodeCategory.DEMO;
    }

    @Override
    public NodeResult execute(NodeContext ctx) {
        Map<String, Object> params = ctx.inputParams();
        log.info("demo-log-print: instance={} node={} attempt={} params={}",
            ctx.getInstanceCode(), ctx.getNodeCode(), ctx.getAttempt(), params);
        return NodeResult.ok(Map.of("echo", params));
    }
}
