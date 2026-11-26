package com.wkclz.micro.dag.handler.demo;

import com.wkclz.micro.dag.api.NodeContext;
import com.wkclz.micro.dag.api.NodeHandler;
import com.wkclz.micro.dag.api.NodeResult;
import com.wkclz.micro.dag.bean.dto.NodeParamDef;
import com.wkclz.micro.dag.bean.enums.NodeCategory;
import com.wkclz.micro.dag.bean.enums.NodeParamType;
import com.wkclz.micro.dag.utils.ParamUtils;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;

/**
 * demo-可控失败：前 N 次返回显式失败，之后成功，演示 fail 语义（实例转 RETRYING）、过程日志(NODE_LOG)与 trace_code 回填
 * 入参：failTimes（失败次数，缺省 0，负数按 0）；msg（失败消息，缺省 simulated failure）
 */
@Slf4j
public class DemoFailHandler implements NodeHandler {

    private static final String DEFAULT_MSG = "simulated failure";

    @Override
    public String nodeCode() {
        return "demo-fail";
    }

    @Override
    public String nodeName() {
        return "演示-可控失败";
    }

    @Override
    public NodeCategory category() {
        return NodeCategory.DEMO;
    }

    @Override
    public NodeResult execute(NodeContext ctx) {
        int failTimes = resolveFailTimes(ctx);
        String msg = resolveMsg(ctx);
        int attempt = ctx.getAttempt();
        log.info("demo-fail: instance={} node={} attempt={} failTimes={}",
            ctx.getInstanceCode(), ctx.getNodeCode(), attempt, failTimes);
        if (attempt < failTimes) {
            ctx.log("attempt=" + attempt + " 模拟失败: " + msg);
            return NodeResult.fail(Map.of("attempt", attempt, "msg", msg));
        }
        ctx.log("attempt=" + attempt + " 执行成功");
        return NodeResult.ok(Map.of("attempt", attempt, "msg", "success"))
            .withTraceCode("demo-trace-" + ctx.getInstanceCode());
    }

    /** 解析 failTimes：缺省 0，负数按 0，非法值抛校验异常 */
    int resolveFailTimes(NodeContext ctx) {
        Object v = ctx.inputParams().get("failTimes");
        if (v == null) {
            return 0;
        }
        int failTimes = ParamUtils.parseIntParam(v, "demo-fail failTimes");
        return Math.max(failTimes, 0);
    }

    /** 解析 msg：可选，空白取缺省值 */
    String resolveMsg(NodeContext ctx) {
        Object v = ctx.inputParams().get("msg");
        if (v instanceof String s && !s.isBlank()) {
            return s;
        }
        return DEFAULT_MSG;
    }

    @Override
    public List<NodeParamDef> paramSchema() {
        return List.of(
            NodeParamDef.of("failTimes", "失败次数(前 N 次失败)", NodeParamType.NUMBER).defaultValue("0"),
            NodeParamDef.of("msg", "失败消息", NodeParamType.TEXT).defaultValue(DEFAULT_MSG)
        );
    }
}
