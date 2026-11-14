package com.wkclz.micro.dag.handler.demo;

import com.wkclz.micro.dag.api.NodeContext;
import com.wkclz.micro.dag.api.NodeHandler;
import com.wkclz.micro.dag.api.NodeResult;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * demo-数据转换：合并入参并追加 processedAt 时间戳，演示入参加工与下游输出
 */
@Slf4j
public class TransformHandler implements NodeHandler {

    @Override
    public String nodeCode() {
        return "demo-transform";
    }

    @Override
    public String nodeName() {
        return "演示-数据转换";
    }

    @Override
    public NodeResult execute(NodeContext ctx) {
        Map<String, Object> params = ctx.inputParams();
        Map<String, Object> output = new HashMap<>(params);
        output.put("processedAt", LocalDateTime.now().toString());
        log.info("demo-transform: instance={} node={} in={} out={}",
            ctx.getInstanceCode(), ctx.getNodeCode(), params, output);
        return NodeResult.ok(output);
    }
}
