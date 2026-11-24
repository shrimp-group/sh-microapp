package com.wkclz.micro.dag.handler.system;

import com.wkclz.micro.dag.api.NodeContext;
import com.wkclz.micro.dag.api.NodeHandler;
import com.wkclz.micro.dag.api.NodeResult;
import com.wkclz.micro.dag.bean.enums.NodeCategory;
import com.wkclz.micro.dag.bean.enums.SystemHandler;
import com.wkclz.micro.dag.core.DagExpressionEvaluator;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * system-branch 分支节点：对出边条件（whenExpr）求值并输出明细
 * 入参：branches（key=下游节点编码 toCode，value=whenExpr 表达式）
 * 输出：{branches: {toCode: bool}, selected: [求值为 true 的 toCode]}；下游就绪判断逻辑不变
 */
@Slf4j
@Component
public class SystemBranchHandler implements NodeHandler {

    @Resource
    private DagExpressionEvaluator evaluator;

    @Override
    public String nodeCode() {
        return SystemHandler.BRANCH.getCode();
    }

    @Override
    public String nodeName() {
        return "系统-分支节点";
    }

    @Override
    public NodeCategory category() {
        return NodeCategory.SYSTEM;
    }

    @Override
    public NodeResult execute(NodeContext ctx) {
        Map<String, Object> params = ctx.inputParams();
        log.info("system-branch: instance={} node={} attempt={}",
            ctx.getInstanceCode(), ctx.getNodeCode(), ctx.getAttempt());
        if (!(params.get("branches") instanceof Map<?, ?> raw) || raw.isEmpty()) {
            log.info("system-branch: 无 branches 参数，输出空明细 instance={}", ctx.getInstanceCode());
            return NodeResult.ok(Map.of("branches", Map.of(), "selected", List.of()));
        }
        Map<String, Object> branches = new LinkedHashMap<>();
        List<String> selected = new ArrayList<>();
        for (Map.Entry<?, ?> entry : raw.entrySet()) {
            String toCode = String.valueOf(entry.getKey());
            try {
                boolean result = evaluator.evaluate(String.valueOf(entry.getValue()), params, Map.of());
                branches.put(toCode, result);
                if (result) {
                    selected.add(toCode);
                }
            } catch (RuntimeException e) {
                log.error("system-branch: 表达式求值失败 toCode={} expr={}", toCode, entry.getValue(), e);
                throw e;
            }
        }
        log.info("system-branch: selected={}", selected);
        return NodeResult.ok(Map.of("branches", branches, "selected", selected));
    }
}
