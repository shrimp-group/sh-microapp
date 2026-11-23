package com.wkclz.micro.dag.core;

import com.wkclz.micro.dag.bean.dto.DagDefModel;
import com.wkclz.micro.dag.bean.dto.EdgeDef;
import com.wkclz.micro.dag.bean.dto.NodeDef;
import com.wkclz.micro.dag.bean.enums.NodeStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * DAG 就绪计算 / 分支级联跳过 / 实例终态计算（纯函数，无状态）
 */
@Slf4j
@Component
public class DagReadyCalculator {

    private final DagExpressionEvaluator evaluator;

    public DagReadyCalculator(DagExpressionEvaluator evaluator) {
        this.evaluator = evaluator;
    }

    /**
     * 计算当前可领取的节点集合：
     * 节点状态为 PENDING，且全部上游 satisfiesDownstream()，
     * 且全部来自 SUCCESS 上游的 CONDITION 边求值为 true（SKIPPED 上游的条件边视为不满足）
     */
    public Set<String> readyNodes(DagDefModel def, Map<String, NodeStatus> statusMap,
                                  Map<String, Object> ctx, Map<String, Map<String, Object>> nodeOutputs) {
        log.info("DagReadyCalculator.readyNodes nodeSize={}", def.getNodes() == null ? 0 : def.getNodes().size());
        Set<String> ready = new HashSet<>();
        for (NodeDef node : def.getNodes()) {
            if (statusMap.get(node.getCode()) != NodeStatus.PENDING) {
                continue;
            }
            boolean allUpstreamSatisfied = true;
            boolean conditionsMet = true;
            for (EdgeDef in : incomingEdges(def, node.getCode())) {
                NodeStatus up = statusMap.get(in.getFromCode());
                if (up == null || !up.satisfiesDownstream()) {
                    allUpstreamSatisfied = false;
                    break;
                }
                if (EdgeDef.TYPE_CONDITION.equals(in.getType()) && up == NodeStatus.SUCCESS) {
                    if (!evaluator.evaluate(in.getWhenExpr(), ctx, nodeOutputs)) {
                        conditionsMet = false;
                        break;
                    }
                }
            }
            if (allUpstreamSatisfied && conditionsMet) {
                ready.add(node.getCode());
            }
        }
        return ready;
    }

    /**
     * 计算需要级联置 SKIPPED 的节点（仅分支语义）：
     * 全部入边均确定性不满足（SUCCESS 上游的条件边求值 false、上游链式 SKIPPED）；
     * 上游为 FAILED/MANUAL_STOPPED/PENDING/RUNNING/RETRYING 属未定状态，下游保持 PENDING 等待，不参与跳过判定
     */
    public Set<String> cascadeSkips(DagDefModel def, Map<String, NodeStatus> statusMap,
                                    Map<String, Object> ctx, Map<String, Map<String, Object>> nodeOutputs) {
        log.info("DagReadyCalculator.cascadeSkips nodeSize={}", def.getNodes() == null ? 0 : def.getNodes().size());
        Set<String> skipped = new HashSet<>();
        Deque<String> queue = new ArrayDeque<>(def.getNodes().stream().map(NodeDef::getCode).toList());
        while (!queue.isEmpty()) {
            String code = queue.poll();
            if (skipped.contains(code) || statusMap.get(code) != NodeStatus.PENDING) {
                continue;
            }
            List<EdgeDef> ins = incomingEdges(def, code);
            if (ins.isEmpty()) {
                continue; // 起始节点不跳过
            }
            boolean anySatisfied = false;
            boolean anyUndetermined = false;
            for (EdgeDef in : ins) {
                NodeStatus up = statusMap.get(in.getFromCode());
                if (skipped.contains(in.getFromCode())) {
                    continue; // 上游被跳过 → 该边不满足
                }
                if (up != null && up.satisfiesDownstream()) {
                    if (EdgeDef.TYPE_CONDITION.equals(in.getType()) && up == NodeStatus.SUCCESS) {
                        if (evaluator.evaluate(in.getWhenExpr(), ctx, nodeOutputs)) {
                            anySatisfied = true;
                            break;
                        }
                    } else {
                        anySatisfied = true;
                        break;
                    }
                } else {
                    anyUndetermined = true; // 上游未定（含 FAILED/MANUAL_STOPPED，保持等待）暂不跳过
                }
            }
            if (!anySatisfied && !anyUndetermined) {
                skipped.add(code);
                queue.addAll(adjacentTargets(def, code)); // 下游重新评估
            }
        }
        return skipped;
    }

    /**
     * 实例终态计算：存在 FAILED/MANUAL_STOPPED 且无在途节点（RUNNING/RETRYING）→ FAILED
     * （下游 PENDING 保持等待，可重跑恢复）；无失败且全部节点终态 → SUCCESS；否则 empty
     */
    public Optional<String> instanceTerminalStatus(Map<String, NodeStatus> statusMap) {
        boolean anyInFlight = statusMap.values().stream()
            .anyMatch(s -> s == NodeStatus.RUNNING || s == NodeStatus.RETRYING);
        if (anyInFlight) {
            return Optional.empty();
        }
        boolean anyFailed = statusMap.values().stream()
            .anyMatch(s -> s == NodeStatus.FAILED || s == NodeStatus.MANUAL_STOPPED);
        if (anyFailed) {
            return Optional.of("FAILED");
        }
        boolean allTerminal = statusMap.values().stream().allMatch(NodeStatus::isTerminal);
        return allTerminal ? Optional.of("SUCCESS") : Optional.empty();
    }

    private List<EdgeDef> incomingEdges(DagDefModel def, String target) {
        List<EdgeDef> ins = new ArrayList<>();
        if (def.getEdges() != null) {
            for (EdgeDef e : def.getEdges()) {
                if (target.equals(e.getToCode())) { ins.add(e); }
            }
        }
        return ins;
    }

    private List<String> adjacentTargets(DagDefModel def, String from) {
        List<String> targets = new ArrayList<>();
        if (def.getEdges() != null) {
            for (EdgeDef e : def.getEdges()) {
                if (from.equals(e.getFromCode())) { targets.add(e.getToCode()); }
            }
        }
        return targets;
    }
}
