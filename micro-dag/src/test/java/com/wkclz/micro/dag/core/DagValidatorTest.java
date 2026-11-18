package com.wkclz.micro.dag.core;

import com.wkclz.micro.dag.bean.dto.DagDefModel;
import com.wkclz.micro.dag.bean.dto.EdgeDef;
import com.wkclz.micro.dag.bean.dto.NodeDef;
import com.wkclz.micro.dag.bean.enums.NodeType;
import com.wkclz.micro.dag.bean.enums.SystemHandler;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 定义校验器单测：环检测 / 悬空依赖 / 条件边缺 when / nodeCode 格式 / 重复编码 / 特殊节点（START/END/BRANCH）
 */
class DagValidatorTest {

    private final DagValidator validator = new DagValidator();

    private NodeDef node(String code) {
        NodeDef n = new NodeDef();
        n.setCode(code);
        n.setName(code);
        n.setHandler("h-" + code);
        return n;
    }

    private NodeDef specialNode(String code, String nodeType) {
        NodeDef n = node(code);
        n.setNodeType(nodeType);
        n.setHandler(null);   // 特殊节点免 handler
        return n;
    }

    private EdgeDef edge(String fromCode, String toCode) {
        EdgeDef e = new EdgeDef();
        e.setEdgeCode(fromCode + "->" + toCode);
        e.setFromCode(fromCode);
        e.setToCode(toCode);
        return e;
    }

    private EdgeDef conditionEdge(String fromCode, String toCode) {
        EdgeDef e = edge(fromCode, toCode);
        e.setType(EdgeDef.TYPE_CONDITION);
        e.setWhenExpr("${ctx.k} == 'v'");
        return e;
    }

    @Test
    void shouldPassValidDag() {
        DagDefModel def = new DagDefModel();
        def.setDagCode("d1");
        def.setDagVersion(1);
        def.setNodes(List.of(node("a"), node("b")));
        def.setEdges(List.of(edge("a", "b")));
        assertTrue(validator.validate(def, Set.of("h-a", "h-b")).isEmpty());
    }

    @Test
    void shouldDetectCycle() {
        DagDefModel def = new DagDefModel();
        def.setDagCode("d1");
        def.setDagVersion(1);
        def.setNodes(List.of(node("a"), node("b"), node("c")));
        def.setEdges(List.of(edge("a", "b"), edge("b", "c"), edge("c", "a")));
        List<String> errors = validator.validate(def, Set.of("h-a", "h-b", "h-c"));
        assertEquals(1, errors.size());
        assertTrue(errors.get(0).contains("环"));
    }

    @Test
    void shouldDetectDanglingDependency() {
        DagDefModel def = new DagDefModel();
        def.setDagCode("d1");
        def.setDagVersion(1);
        def.setNodes(List.of(node("a")));
        def.setEdges(List.of(edge("ghost", "a")));
        List<String> errors = validator.validate(def, Set.of("h-a"));
        assertEquals(1, errors.size());
        assertTrue(errors.get(0).contains("ghost"));
    }

    @Test
    void shouldDetectConditionEdgeWithoutWhen() {
        DagDefModel def = new DagDefModel();
        def.setDagCode("d1");
        def.setDagVersion(1);
        def.setNodes(List.of(node("a"), node("b")));
        EdgeDef e = edge("a", "b");
        e.setType(EdgeDef.TYPE_CONDITION);
        def.setEdges(List.of(e));
        List<String> errors = validator.validate(def, Set.of("h-a", "h-b"));
        assertEquals(1, errors.size());
        assertTrue(errors.get(0).contains("when"));
    }

    @Test
    void shouldDetectUnknownHandler() {
        DagDefModel def = new DagDefModel();
        def.setDagCode("d1");
        def.setDagVersion(1);
        def.setNodes(List.of(node("a")));
        def.setEdges(List.of());
        List<String> errors = validator.validate(def, Set.of("h-other"));
        assertEquals(1, errors.size());
        assertTrue(errors.get(0).contains("h-a"));
    }

    @Test
    void shouldDetectDuplicateAndBadCode() {
        DagDefModel def = new DagDefModel();
        def.setDagCode("d1");
        def.setDagVersion(1);
        def.setNodes(List.of(node("a"), node("A")));   // 唯一化按小写比较，A 与 a 冲突
        def.setEdges(List.of());
        List<String> errors = validator.validate(def, Set.of("h-a"));
        assertEquals(1, errors.size());
        assertTrue(errors.get(0).contains("重复"));
    }

    @Test
    void shouldDetectBadNodeCodeFormat() {
        DagDefModel def = new DagDefModel();
        def.setDagCode("d1");
        def.setDagVersion(1);
        NodeDef n = node("Bad_Code");                  // 只允许 [a-z0-9-]
        def.setNodes(List.of(n));
        def.setEdges(List.of());
        List<String> errors = validator.validate(def, Set.of("h-Bad_Code"));
        assertEquals(1, errors.size());
        assertTrue(errors.get(0).contains("Bad_Code"));
    }

    @Test
    void shouldPassSpecialNodeWithoutHandler() {
        DagDefModel def = new DagDefModel();
        def.setDagCode("d1");
        def.setDagVersion(1);
        def.setNodes(List.of(specialNode("start", NodeType.START.getCode()), node("a")));
        def.setEdges(List.of(edge("start", "a")));
        assertTrue(validator.validate(def, Set.of("h-a")).isEmpty());
    }

    @Test
    void shouldRejectStartWithInboundEdge() {
        DagDefModel def = new DagDefModel();
        def.setDagCode("d1");
        def.setDagVersion(1);
        def.setNodes(List.of(specialNode("start", NodeType.START.getCode()), node("x"), node("y")));
        def.setEdges(List.of(edge("x", "start"), edge("start", "y")));
        List<String> errors = validator.validate(def, Set.of("h-x", "h-y"));
        assertEquals(1, errors.size());
        assertTrue(errors.get(0).contains("开始节点不能有入边"));
    }

    @Test
    void shouldRejectStartWithoutOutboundEdge() {
        DagDefModel def = new DagDefModel();
        def.setDagCode("d1");
        def.setDagVersion(1);
        def.setNodes(List.of(specialNode("start", NodeType.START.getCode()), node("a")));
        def.setEdges(List.of());
        List<String> errors = validator.validate(def, Set.of("h-a"));
        assertEquals(1, errors.size());
        assertTrue(errors.get(0).contains("开始节点必须连出下游"));
    }

    @Test
    void shouldRejectEndWithOutboundEdge() {
        DagDefModel def = new DagDefModel();
        def.setDagCode("d1");
        def.setDagVersion(1);
        def.setNodes(List.of(node("a"), node("b"), specialNode("end", NodeType.END.getCode())));
        def.setEdges(List.of(edge("a", "end"), edge("end", "b")));
        List<String> errors = validator.validate(def, Set.of("h-a", "h-b"));
        assertEquals(1, errors.size());
        assertTrue(errors.get(0).contains("结束节点不能连出下游"));
    }

    @Test
    void shouldRejectBranchWithNormalOutboundEdge() {
        DagDefModel def = new DagDefModel();
        def.setDagCode("d1");
        def.setDagVersion(1);
        def.setNodes(List.of(specialNode("br", NodeType.BRANCH.getCode()), node("a"), node("b")));
        def.setEdges(List.of(edge("br", "a"), conditionEdge("br", "b")));
        List<String> errors = validator.validate(def, Set.of("h-a", "h-b"));
        assertEquals(1, errors.size());
        assertTrue(errors.get(0).contains("分支节点的出边必须为条件分支"));
    }

    @Test
    void shouldPassBranchWithAllConditionEdges() {
        DagDefModel def = new DagDefModel();
        def.setDagCode("d1");
        def.setDagVersion(1);
        def.setNodes(List.of(specialNode("br", NodeType.BRANCH.getCode()), node("a"), node("b")));
        def.setEdges(List.of(conditionEdge("br", "a"), conditionEdge("br", "b")));
        assertTrue(validator.validate(def, Set.of("h-a", "h-b")).isEmpty());
    }

    @Test
    void shouldRejectIllegalNodeType() {
        DagDefModel def = new DagDefModel();
        def.setDagCode("d1");
        def.setDagVersion(1);
        NodeDef n = node("a");
        n.setNodeType("FOO");
        def.setNodes(List.of(n));
        def.setEdges(List.of());
        List<String> errors = validator.validate(def, Set.of("h-a"));
        assertEquals(1, errors.size());
        assertTrue(errors.get(0).contains("nodeType 非法"));
    }

    // ===== 第 7 项：nodeType 与 handler 匹配校验 =====

    @Test
    void shouldPassStartWithSystemStartHandler() {
        DagDefModel def = new DagDefModel();
        def.setDagCode("d1");
        def.setDagVersion(1);
        NodeDef start = specialNode("start", NodeType.START.getCode());
        start.setHandler(SystemHandler.START.getCode());
        def.setNodes(List.of(start, node("a")));
        def.setEdges(List.of(edge("start", "a")));
        assertTrue(validator.validate(def, Set.of("h-a", SystemHandler.START.getCode())).isEmpty());
    }

    @Test
    void shouldRejectStartWithEndHandler() {
        DagDefModel def = new DagDefModel();
        def.setDagCode("d1");
        def.setDagVersion(1);
        NodeDef start = specialNode("start", NodeType.START.getCode());
        start.setHandler(SystemHandler.END.getCode());
        def.setNodes(List.of(start, node("a")));
        def.setEdges(List.of(edge("start", "a")));
        List<String> errors = validator.validate(def, Set.of("h-a", SystemHandler.END.getCode()));
        assertEquals(1, errors.size());
        assertTrue(errors.get(0).contains("nodeType 与 handler 不匹配"));
    }

    @Test
    void shouldPassBranchWithSystemBranchHandler() {
        DagDefModel def = new DagDefModel();
        def.setDagCode("d1");
        def.setDagVersion(1);
        NodeDef br = specialNode("br", NodeType.BRANCH.getCode());
        br.setHandler(SystemHandler.BRANCH.getCode());
        def.setNodes(List.of(br, node("a"), node("b")));
        def.setEdges(List.of(conditionEdge("br", "a"), conditionEdge("br", "b")));
        assertTrue(validator.validate(def, Set.of("h-a", "h-b", SystemHandler.BRANCH.getCode())).isEmpty());
    }

    @Test
    void shouldPassNormalWithSystemTimerHandler() {
        DagDefModel def = new DagDefModel();
        def.setDagCode("d1");
        def.setDagVersion(1);
        NodeDef t = node("t");
        t.setHandler(SystemHandler.TIMER.getCode());
        def.setNodes(List.of(t));
        def.setEdges(List.of());
        assertTrue(validator.validate(def, Set.of(SystemHandler.TIMER.getCode())).isEmpty());
    }

    @Test
    void shouldRejectStartWithTimerHandler() {
        DagDefModel def = new DagDefModel();
        def.setDagCode("d1");
        def.setDagVersion(1);
        NodeDef start = specialNode("start", NodeType.START.getCode());
        start.setHandler(SystemHandler.TIMER.getCode());   // system-timer 仅限 NORMAL 节点
        def.setNodes(List.of(start, node("a")));
        def.setEdges(List.of(edge("start", "a")));
        List<String> errors = validator.validate(def, Set.of("h-a", SystemHandler.TIMER.getCode()));
        assertEquals(1, errors.size());
        assertTrue(errors.get(0).contains("nodeType 与 handler 不匹配"));
    }

    @Test
    void shouldRejectNormalWithSystemStartHandler() {
        DagDefModel def = new DagDefModel();
        def.setDagCode("d1");
        def.setDagVersion(1);
        NodeDef n = node("a");
        n.setHandler(SystemHandler.START.getCode());       // NORMAL 节点不能使用 system-start
        def.setNodes(List.of(n));
        def.setEdges(List.of());
        List<String> errors = validator.validate(def, Set.of(SystemHandler.START.getCode()));
        assertEquals(1, errors.size());
        assertTrue(errors.get(0).contains("nodeType 与 handler 不匹配"));
    }

    @Test
    void shouldRejectSpecialNodeWithUnregisteredHandler() {
        DagDefModel def = new DagDefModel();
        def.setDagCode("d1");
        def.setDagVersion(1);
        NodeDef start = specialNode("start", NodeType.START.getCode());
        start.setHandler("ghost-handler");         // 特殊节点带 handler 也需已注册
        def.setNodes(List.of(start, node("a")));
        def.setEdges(List.of(edge("start", "a")));
        List<String> errors = validator.validate(def, Set.of("h-a"));
        assertEquals(2, errors.size());
        assertTrue(errors.stream().anyMatch(e -> e.contains("未注册")));
        assertTrue(errors.stream().anyMatch(e -> e.contains("nodeType 与 handler 不匹配")));
    }

    // ===== START 时间驱动：triggerType=TIMER 时 cron 必填且可解析 =====

    @Test
    void shouldRejectTimerTriggerWithoutCron() {
        DagDefModel def = new DagDefModel();
        def.setDagCode("d1");
        def.setDagVersion(1);
        NodeDef start = specialNode("start", NodeType.START.getCode());
        start.setParams(Map.of("triggerType", "TIMER"));
        def.setNodes(List.of(start, node("a")));
        def.setEdges(List.of(edge("start", "a")));
        List<String> errors = validator.validate(def, Set.of("h-a"));
        assertEquals(1, errors.size());
        assertTrue(errors.get(0).contains("cron 必填"));
    }

    @Test
    void shouldRejectTimerTriggerWithBadCron() {
        DagDefModel def = new DagDefModel();
        def.setDagCode("d1");
        def.setDagVersion(1);
        NodeDef start = specialNode("start", NodeType.START.getCode());
        start.setParams(Map.of("triggerType", "timer", "cron", "* * * *"));   // 触发类型忽略大小写；cron 域数不足
        def.setNodes(List.of(start, node("a")));
        def.setEdges(List.of(edge("start", "a")));
        List<String> errors = validator.validate(def, Set.of("h-a"));
        assertEquals(1, errors.size());
        assertTrue(errors.get(0).contains("触发 cron 非法"));
    }

    @Test
    void shouldPassTimerTriggerWithValidCron() {
        DagDefModel def = new DagDefModel();
        def.setDagCode("d1");
        def.setDagVersion(1);
        NodeDef start = specialNode("start", NodeType.START.getCode());
        start.setParams(Map.of("triggerType", "TIMER", "cron", "0 0 2 * * *"));
        def.setNodes(List.of(start, node("a")));
        def.setEdges(List.of(edge("start", "a")));
        assertTrue(validator.validate(def, Set.of("h-a")).isEmpty());
    }

    @Test
    void shouldIgnoreTimerCheckForNonStartNode() {
        DagDefModel def = new DagDefModel();
        def.setDagCode("d1");
        def.setDagVersion(1);
        NodeDef n = node("a");
        n.setParams(Map.of("triggerType", "TIMER", "cron", "* * * *"));   // 非 START 节点不做 TIMER cron 校验
        def.setNodes(List.of(n));
        def.setEdges(List.of());
        assertTrue(validator.validate(def, Set.of("h-a")).isEmpty());
    }
}
