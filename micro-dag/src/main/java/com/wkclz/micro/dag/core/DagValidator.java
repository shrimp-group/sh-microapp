package com.wkclz.micro.dag.core;

import com.wkclz.micro.dag.bean.dto.DagDefModel;
import com.wkclz.micro.dag.bean.dto.EdgeDef;
import com.wkclz.micro.dag.bean.dto.NodeDef;
import com.wkclz.micro.dag.bean.enums.NodeType;
import com.wkclz.micro.dag.bean.enums.SystemHandler;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.scheduling.support.CronExpression;
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
        // 3. handler 必须已注册（特殊节点 handler 为空时免 handler 直达成功；带 handler 的特殊节点仍需已注册）
        for (NodeDef n : def.getNodes()) {
            if (NodeType.specialOf(n.getNodeType()) != null && StringUtils.isBlank(n.getHandler())) {
                continue;
            }
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
        // 6. 特殊节点（START/END/BRANCH）：nodeType 合法性 + 拓扑约束
        for (NodeDef n : def.getNodes()) {
            NodeType type = NodeType.ofCode(n.getNodeType());
            if (n.getNodeType() != null && type == null) {
                errors.add("节点 " + n.getCode() + " 的 nodeType 非法：" + n.getNodeType());
                continue;
            }
            List<EdgeDef> outs = edges.stream().filter(e -> n.getCode().equals(e.getFromCode())).toList();
            List<EdgeDef> ins = edges.stream().filter(e -> n.getCode().equals(e.getToCode())).toList();
            switch (type == null ? NodeType.NORMAL : type) {
                case START -> {
                    if (!ins.isEmpty()) { errors.add("开始节点不能有入边：" + n.getCode()); }
                    if (outs.isEmpty()) { errors.add("开始节点必须连出下游：" + n.getCode()); }
                }
                case END -> {
                    if (!outs.isEmpty()) { errors.add("结束节点不能连出下游：" + n.getCode()); }
                }
                case BRANCH -> {
                    if (outs.isEmpty()) { errors.add("分支节点必须连出下游：" + n.getCode()); }
                    if (!outs.stream().allMatch(e -> EdgeDef.TYPE_CONDITION.equals(e.getType()))) {
                        errors.add("分支节点的出边必须为条件分支：" + n.getCode());
                    }
                }
                case NORMAL -> { }
            }
        }
        // 7. nodeType 与 handler 匹配（START↔system-start、END↔system-end、BRANCH↔system-branch、system-timer 仅限 NORMAL）
        for (NodeDef n : def.getNodes()) {
            // 非法 nodeType 已在规则 6 报错，此处按缺省 NORMAL 处理避免 NPE
            NodeType t = NodeType.ofCode(n.getNodeType());
            String type = (t == null ? NodeType.NORMAL : t).getCode();
            String handler = n.getHandler();
            if (StringUtils.isBlank(handler)) {
                continue;
            }
            SystemHandler sh = SystemHandler.of(handler);
            if (sh != null) {
                if (!sh.getNodeType().getCode().equals(type)) {
                    errors.add("节点 " + n.getCode() + " 的 nodeType 与 handler 不匹配：" + handler + " 仅限 " + sh.getNodeType().getCode() + " 节点");
                }
                continue;
            }
            NodeType sn = NodeType.specialOf(type);
            if (sn != null) {
                errors.add("节点 " + n.getCode() + " 的 nodeType 与 handler 不匹配：" + sn.getCode() + " 节点不能使用 " + handler);
            }
        }
        // 8. START 时间驱动：params.triggerType=TIMER 时 cron 必填且可解析（Spring 6 域 cron）
        for (NodeDef n : def.getNodes()) {
            String type = n.getNodeType() == null ? NodeType.NORMAL.getCode() : n.getNodeType();
            if (!NodeType.START.getCode().equals(type) || n.getParams() == null || n.getParams().isEmpty()) {
                continue;
            }
            Object triggerType = n.getParams().get("triggerType");
            if (triggerType == null || !"TIMER".equalsIgnoreCase(String.valueOf(triggerType))) {
                continue;
            }
            Object cron = n.getParams().get("cron");
            if (cron == null || String.valueOf(cron).isBlank()) {
                errors.add("节点 " + n.getCode() + " 的触发 cron 必填");
                continue;
            }
            try {
                CronExpression.parse(String.valueOf(cron));
            } catch (IllegalArgumentException e) {
                log.error("DagValidator: cron 解析失败 nodeCode={} cron={}", n.getCode(), cron, e);
                errors.add("节点 " + n.getCode() + " 的触发 cron 非法：" + e.getMessage());
            }
        }
        log.info("DagValidator.validate done errors={}", errors);
        return errors;
    }
}
