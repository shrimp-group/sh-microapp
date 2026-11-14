package com.wkclz.micro.dag.core;

import com.wkclz.micro.dag.bean.dto.DagDefModel;
import com.wkclz.micro.dag.bean.dto.EdgeDef;
import com.wkclz.micro.dag.bean.dto.NodeDef;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * DAG 定义校验器：结构合法性 + 环检测（ Kahn 拓扑排序，无法完成排序即有环 ）
 * 校验结果为问题清单（面向设计器展示），不抛异常
 */
@Slf4j
@Component
public class DagValidator {

    private static final String CODE_PATTERN = "[a-z0-9-]+";

    public List<String> validate(DagDefModel def, Set<String> registeredHandlers) {
        log.info("DagValidator.validate dagCode={}", def == null ? null : def.getDagCode());
        List<String> errors = new ArrayList<>();
        if (def == null || def.getNodes() == null || def.getNodes().isEmpty()) {
            errors.add("节点列表不能为空");
            return errors;
        }
        // 1. nodeCode 重复（唯一化按小写比较）
        Map<String, NodeDef> nodeMap = new HashMap<>();
        for (NodeDef n : def.getNodes()) {
            String key = n.getCode() == null ? "" : n.getCode().toLowerCase();
            if (key.isEmpty()) {
                errors.add("节点编码不能为空");
                continue;
            }
            if (nodeMap.putIfAbsent(key, n) != null) {
                errors.add("节点编码重复：" + n.getCode());
            }
        }
        if (!errors.isEmpty()) {
            return errors;
        }
        // 2. nodeCode 格式（仅允许小写字母/数字/中划线）
        for (NodeDef n : def.getNodes()) {
            if (!n.getCode().matches(CODE_PATTERN)) {
                errors.add("节点编码非法（仅允许小写字母/数字/中划线）：" + n.getCode());
            }
        }
        if (!errors.isEmpty()) {
            return errors;
        }
        // 3. handler 必须已注册
        for (NodeDef n : def.getNodes()) {
            if (n.getHandler() == null || !registeredHandlers.contains(n.getHandler())) {
                errors.add("节点 " + n.getCode() + " 的 handler 未注册：" + n.getHandler());
            }
        }
        // 4. 边结构：端点存在、条件边必须有 whenExpr
        List<EdgeDef> edges = def.getEdges() == null ? List.of() : def.getEdges();
        for (EdgeDef e : edges) {
            if (e.getFromCode() == null || !nodeMap.containsKey(e.getFromCode())
                || e.getToCode() == null || !nodeMap.containsKey(e.getToCode())) {
                errors.add("边存在悬空依赖：" + e.getFromCode() + " -> " + e.getToCode());
                continue;
            }
            if (EdgeDef.TYPE_CONDITION.equals(e.getType())
                && (e.getWhenExpr() == null || e.getWhenExpr().isBlank())) {
                errors.add("条件边缺少 whenExpr 表达式：" + e.getFromCode() + " -> " + e.getToCode());
            }
        }
        // 5. 环检测（Kahn）
        Map<String, Integer> inDegree = new HashMap<>();
        Map<String, List<String>> adj = new HashMap<>();
        for (String code : nodeMap.keySet()) {
            inDegree.put(code, 0);
            adj.put(code, new ArrayList<>());
        }
        Set<String> seenEdges = new HashSet<>();
        for (EdgeDef e : edges) {
            if (!nodeMap.containsKey(e.getFromCode()) || !nodeMap.containsKey(e.getToCode())) {
                continue; // 悬空依赖已在上面报错
            }
            if (seenEdges.add(e.getFromCode() + "->" + e.getToCode())) {
                adj.get(e.getFromCode()).add(e.getToCode());
                inDegree.merge(e.getToCode(), 1, Integer::sum);
            }
        }
        List<String> zero = new ArrayList<>();
        inDegree.forEach((k, v) -> { if (v == 0) { zero.add(k); } });
        int visited = 0;
        while (!zero.isEmpty()) {
            String cur = zero.removeLast();
            visited++;
            for (String next : adj.get(cur)) {
                int left = inDegree.merge(next, -1, Integer::sum);
                if (left == 0) { zero.add(next); }
            }
        }
        if (visited < nodeMap.size()) {
            List<String> cycleNodes = new ArrayList<>();
            inDegree.forEach((k, v) -> { if (v > 0) { cycleNodes.add(k); } });
            errors.add("定义存在环，涉及节点：" + cycleNodes);
        }
        log.info("DagValidator.validate done errors={}", errors);
        return errors;
    }
}
