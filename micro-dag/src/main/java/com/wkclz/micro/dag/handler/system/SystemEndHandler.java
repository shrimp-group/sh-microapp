package com.wkclz.micro.dag.handler.system;

import com.wkclz.micro.dag.api.NodeContext;
import com.wkclz.micro.dag.api.NodeHandler;
import com.wkclz.micro.dag.api.NodeResult;
import com.wkclz.micro.dag.bean.enums.NodeCategory;
import com.wkclz.micro.dag.bean.enums.SystemHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * system-end 结束节点：直接成功，输出为空
 */
@Slf4j
@Component
public class SystemEndHandler implements NodeHandler {

    @Override
    public String nodeCode() {
        return SystemHandler.END.getCode();
    }

    @Override
    public String nodeName() {
        return "系统-结束节点";
    }

    @Override
    public NodeCategory category() {
        return NodeCategory.SYSTEM;
    }

    @Override
    public NodeResult execute(NodeContext ctx) {
        log.info("system-end: instance={} node={} attempt={}",
            ctx.getInstanceCode(), ctx.getNodeCode(), ctx.getAttempt());
        return NodeResult.ok(Map.of());
    }
}
