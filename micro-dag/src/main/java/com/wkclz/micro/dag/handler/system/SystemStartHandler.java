package com.wkclz.micro.dag.handler.system;

import com.wkclz.micro.dag.api.NodeContext;
import com.wkclz.micro.dag.api.NodeHandler;
import com.wkclz.micro.dag.api.NodeResult;
import com.wkclz.micro.dag.bean.dto.NodeParamDef;
import com.wkclz.micro.dag.bean.enums.NodeCategory;
import com.wkclz.micro.dag.bean.enums.NodeParamType;
import com.wkclz.micro.dag.bean.enums.SystemHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * system-start 开始节点：透传节点入参作为输出，供下游 ${out.&lt;node&gt;.*} 引用
 */
@Slf4j
@Component
public class SystemStartHandler implements NodeHandler {

    @Override
    public String nodeCode() {
        return SystemHandler.START.getCode();
    }

    @Override
    public String nodeName() {
        return "系统-开始节点";
    }

    @Override
    public NodeCategory category() {
        return NodeCategory.SYSTEM;
    }

    @Override
    public NodeResult execute(NodeContext ctx) {
        Map<String, Object> params = ctx.inputParams();
        log.info("system-start: instance={} node={} attempt={} params={}",
            ctx.getInstanceCode(), ctx.getNodeCode(), ctx.getAttempt(), params);
        return NodeResult.ok(params);
    }

    @Override
    public List<NodeParamDef> paramSchema() {
        return List.of(
            NodeParamDef.of("triggerType", "触发方式", NodeParamType.SELECT)
                .options(new NodeParamDef.Option("MANUAL", "手动"), new NodeParamDef.Option("TIMER", "定时"))
                .defaultValue("MANUAL").help("TIMER 时按 cron 定时自动发起实例"),
            NodeParamDef.of("cron", "触发 cron", NodeParamType.CRON)
                .requiredWhen("triggerType", "TIMER")
                .placeholder("0 0 2 * * *（Spring 6 域）"),
            NodeParamDef.of("triggerParams", "触发参数", NodeParamType.KEY_VALUE)
                .help("定时发起实例时的入参")
        );
    }
}
