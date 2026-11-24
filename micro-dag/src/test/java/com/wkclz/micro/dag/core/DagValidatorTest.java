package com.wkclz.micro.dag.core;

import com.wkclz.micro.dag.api.NodeContext;
import com.wkclz.micro.dag.api.NodeHandler;
import com.wkclz.micro.dag.api.NodeHandlerRegistry;
import com.wkclz.micro.dag.api.NodeResult;
import com.wkclz.micro.dag.bean.constant.DagConstants;
import com.wkclz.micro.dag.bean.dto.DagDefModel;
import com.wkclz.micro.dag.bean.dto.EdgeDef;
import com.wkclz.micro.dag.bean.dto.NodeDef;
import com.wkclz.micro.dag.bean.enums.NodeType;
import com.wkclz.micro.dag.bean.enums.SystemHandler;
import com.wkclz.micro.dag.handler.system.SystemStartHandler;
import com.wkclz.micro.dag.handler.system.SystemTimerHandler;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 定义校验器单测：环检测 / 悬空依赖 / 条件边缺 when / nodeCode 格式 / 重复编码 / 入口汇点约束（唯一 START/END）
 * / 特殊节点（START/END/BRANCH）/ nodeType-handler 匹配 / START 时间驱动 cron / 节点参数 schema 校验
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
        e.setType(DagConstants.EDGE_TYPE_CONDITION);
        e.setWhenExpr("${ctx.k} == 'v'");
        return e;
    }

    /** 匿名 handler：仅为注册占位，无执行语义 */
    private NodeHandler handlerOf(String code) {
        return new NodeHandler() {
            @Override
            public String nodeCode() { return code; }
            @Override
            public NodeResult execute(NodeContext ctx) {
                return NodeResult.ok(Map.of());
            }
        };
    }

    /** 按编码集合构建注册表，替代原 Set<String> 入参 */
    private NodeHandlerRegistry registry(String... codes) {
        NodeHandlerRegistry registry = new NodeHandlerRegistry();
        ReflectionTestUtils.setField(registry, "handlerList",
            Arrays.stream(codes).map(this::handlerOf).toList());
        registry.init();
        return registry;
    }

    @Test
    void shouldPassValidDag() {
        DagDefModel def = new DagDefModel();
        def.setDagCode("d1");
        def.setDagVersion(1);
        def.setNodes(List.of(specialNode("start", NodeType.START.getCode()), node("a"), node("b"),
            specialNode("end", NodeType.END.getCode())));
        def.setEdges(List.of(edge("start", "a"), edge("a", "b"), edge("b", "end")));
        assertTrue(validator.validate(def, registry("h-a", "h-b")).isEmpty());
    }

    // ===== 第 6 项：入口/汇点约束（唯一 START/END） =====

    @Test
    void shouldRejectMissingStart() {
        DagDefModel def = new DagDefModel();
        def.setDagCode("d1");
        def.setDagVersion(1);
        // 无 START 时 NORMAL 入口必然并存"节点不在开始位置"
        def.setNodes(List.of(node("a"), specialNode("end", NodeType.END.getCode())));
        def.setEdges(List.of(edge("a", "end")));
        List<String> errors = validator.validate(def, registry("h-a"));
        assertEquals(2, errors.size());
        assertTrue(errors.stream().anyMatch(e -> e.contains("缺少开始节点（nodeType=START）")));
        assertTrue(errors.stream().anyMatch(e -> e.contains("节点不在开始位置")));
    }

    @Test
    void shouldRejectMissingEnd() {
        DagDefModel def = new DagDefModel();
        def.setDagCode("d1");
        def.setDagVersion(1);
        // 无 END 时 NORMAL 汇点必然并存"节点不在结束位置"
        def.setNodes(List.of(specialNode("start", NodeType.START.getCode()), node("a")));
        def.setEdges(List.of(edge("start", "a")));
        List<String> errors = validator.validate(def, registry("h-a"));
        assertEquals(2, errors.size());
        assertTrue(errors.stream().anyMatch(e -> e.contains("缺少结束节点（nodeType=END）")));
        assertTrue(errors.stream().anyMatch(e -> e.contains("节点不在结束位置")));
    }

    @Test
    void shouldRejectMultipleStarts() {
        DagDefModel def = new DagDefModel();
        def.setDagCode("d1");
        def.setDagVersion(1);
        def.setNodes(List.of(specialNode("s1", NodeType.START.getCode()), specialNode("s2", NodeType.START.getCode()),
            node("a"), specialNode("end", NodeType.END.getCode())));
        def.setEdges(List.of(edge("s1", "a"), edge("s2", "a"), edge("a", "end")));
        List<String> errors = validator.validate(def, registry("h-a"));
        assertEquals(1, errors.size());
        assertTrue(errors.get(0).contains("开始节点只能有一个"));
        assertTrue(errors.get(0).contains("s1"));
        assertTrue(errors.get(0).contains("s2"));
    }

    @Test
    void shouldRejectMultipleEnds() {
        DagDefModel def = new DagDefModel();
        def.setDagCode("d1");
        def.setDagVersion(1);
        def.setNodes(List.of(specialNode("start", NodeType.START.getCode()), node("a"),
            specialNode("e1", NodeType.END.getCode()), specialNode("e2", NodeType.END.getCode())));
        def.setEdges(List.of(edge("start", "a"), edge("a", "e1"), edge("a", "e2")));
        List<String> errors = validator.validate(def, registry("h-a"));
        assertEquals(1, errors.size());
        assertTrue(errors.get(0).contains("结束节点只能有一个"));
    }

    @Test
    void shouldRejectNonStartEntry() {
        DagDefModel def = new DagDefModel();
        def.setDagCode("d1");
        def.setDagVersion(1);
        def.setNodes(List.of(specialNode("start", NodeType.START.getCode()), node("a"), node("b"),
            specialNode("end", NodeType.END.getCode())));
        def.setEdges(List.of(edge("start", "a"), edge("b", "a"), edge("a", "end")));
        List<String> errors = validator.validate(def, registry("h-a", "h-b"));
        assertEquals(1, errors.size());
        assertTrue(errors.get(0).contains("节点不在开始位置（存在入度为 0 的非开始节点）"));
        assertTrue(errors.get(0).contains("b"));
    }

    @Test
    void shouldRejectNonEndSink() {
        DagDefModel def = new DagDefModel();
        def.setDagCode("d1");
        def.setDagVersion(1);
        def.setNodes(List.of(specialNode("start", NodeType.START.getCode()), node("a"), node("b"),
            specialNode("end", NodeType.END.getCode())));
        def.setEdges(List.of(edge("start", "a"), edge("a", "b"), edge("a", "end")));
        List<String> errors = validator.validate(def, registry("h-a", "h-b"));
        assertEquals(1, errors.size());
        assertTrue(errors.get(0).contains("节点不在结束位置（存在出度为 0 的非结束节点）"));
        assertTrue(errors.get(0).contains("b"));
    }

    @Test
    void shouldPassLegalStartEndChain() {
        DagDefModel def = new DagDefModel();
        def.setDagCode("d1");
        def.setDagVersion(1);
        NodeDef start = specialNode("start", NodeType.START.getCode());
        start.setHandler(SystemHandler.START.getCode());
        NodeDef end = specialNode("end", NodeType.END.getCode());
        end.setHandler(SystemHandler.END.getCode());
        def.setNodes(List.of(start, node("a"), end));
        def.setEdges(List.of(edge("start", "a"), edge("a", "end")));
        assertTrue(validator.validate(def,
            registry(SystemHandler.START.getCode(), "h-a", SystemHandler.END.getCode())).isEmpty());
    }

    @Test
    void shouldDetectCycle() {
        DagDefModel def = new DagDefModel();
        def.setDagCode("d1");
        def.setDagVersion(1);
        def.setNodes(List.of(node("a"), node("b"), node("c")));
        def.setEdges(List.of(edge("a", "b"), edge("b", "c"), edge("c", "a")));
        List<String> errors = validator.validate(def, registry("h-a", "h-b", "h-c"));
        // 环内节点入度/出度均非 0，仅并存缺少 START/END 两条错误
        assertEquals(3, errors.size());
        assertTrue(errors.get(0).contains("环"));
    }

    @Test
    void shouldDetectDanglingDependency() {
        DagDefModel def = new DagDefModel();
        def.setDagCode("d1");
        def.setDagVersion(1);
        def.setNodes(List.of(specialNode("start", NodeType.START.getCode()), node("a"),
            specialNode("end", NodeType.END.getCode())));
        def.setEdges(List.of(edge("ghost", "a"), edge("start", "a"), edge("a", "end")));
        List<String> errors = validator.validate(def, registry("h-a"));
        assertEquals(1, errors.size());
        assertTrue(errors.get(0).contains("ghost"));
    }

    @Test
    void shouldDetectConditionEdgeWithoutWhen() {
        DagDefModel def = new DagDefModel();
        def.setDagCode("d1");
        def.setDagVersion(1);
        def.setNodes(List.of(specialNode("start", NodeType.START.getCode()), node("a"), node("b"),
            specialNode("end", NodeType.END.getCode())));
        EdgeDef e = edge("a", "b");
        e.setType(DagConstants.EDGE_TYPE_CONDITION);
        def.setEdges(List.of(edge("start", "a"), e, edge("b", "end")));
        List<String> errors = validator.validate(def, registry("h-a", "h-b"));
        assertEquals(1, errors.size());
        assertTrue(errors.get(0).contains("when"));
    }

    @Test
    void shouldDetectUnknownHandler() {
        DagDefModel def = new DagDefModel();
        def.setDagCode("d1");
        def.setDagVersion(1);
        def.setNodes(List.of(specialNode("start", NodeType.START.getCode()), node("a"),
            specialNode("end", NodeType.END.getCode())));
        def.setEdges(List.of(edge("start", "a"), edge("a", "end")));
        List<String> errors = validator.validate(def, registry("h-other"));
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
        List<String> errors = validator.validate(def, registry("h-a"));
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
        List<String> errors = validator.validate(def, registry("h-Bad_Code"));
        assertEquals(1, errors.size());
        assertTrue(errors.get(0).contains("Bad_Code"));
    }

    @Test
    void shouldPassSpecialNodeWithoutHandler() {
        DagDefModel def = new DagDefModel();
        def.setDagCode("d1");
        def.setDagVersion(1);
        def.setNodes(List.of(specialNode("start", NodeType.START.getCode()), node("a"),
            specialNode("end", NodeType.END.getCode())));
        def.setEdges(List.of(edge("start", "a"), edge("a", "end")));
        assertTrue(validator.validate(def, registry("h-a")).isEmpty());
    }

    @Test
    void shouldRejectStartWithInboundEdge() {
        DagDefModel def = new DagDefModel();
        def.setDagCode("d1");
        def.setDagVersion(1);
        def.setNodes(List.of(specialNode("start", NodeType.START.getCode()), node("x"), node("y"),
            specialNode("end", NodeType.END.getCode())));
        def.setEdges(List.of(edge("x", "start"), edge("start", "y"), edge("y", "end")));
        List<String> errors = validator.validate(def, registry("h-x", "h-y"));
        // x 无上游：入口约束错误与局部约束错误并存
        assertEquals(2, errors.size());
        assertTrue(errors.stream().anyMatch(e -> e.contains("开始节点不能有入边")));
    }

    @Test
    void shouldRejectStartWithoutOutboundEdge() {
        DagDefModel def = new DagDefModel();
        def.setDagCode("d1");
        def.setDagVersion(1);
        def.setNodes(List.of(specialNode("start", NodeType.START.getCode()), node("a"),
            specialNode("end", NodeType.END.getCode())));
        def.setEdges(List.of(edge("a", "end")));
        List<String> errors = validator.validate(def, registry("h-a"));
        // start 无出边：a 成非 START 入口、start 自身成非 END 汇点，与局部约束错误并存
        assertEquals(3, errors.size());
        assertTrue(errors.stream().anyMatch(e -> e.contains("开始节点必须连出下游")));
    }

    @Test
    void shouldRejectEndWithOutboundEdge() {
        DagDefModel def = new DagDefModel();
        def.setDagCode("d1");
        def.setDagVersion(1);
        def.setNodes(List.of(node("a"), node("b"), specialNode("end", NodeType.END.getCode())));
        def.setEdges(List.of(edge("a", "end"), edge("end", "b")));
        List<String> errors = validator.validate(def, registry("h-a", "h-b"));
        // 无 START 且 b 成非 END 汇点：入口/汇点约束错误与局部约束错误并存
        assertEquals(4, errors.size());
        assertTrue(errors.stream().anyMatch(e -> e.contains("结束节点不能连出下游")));
    }

    @Test
    void shouldRejectBranchWithNormalOutboundEdge() {
        DagDefModel def = new DagDefModel();
        def.setDagCode("d1");
        def.setDagVersion(1);
        def.setNodes(List.of(specialNode("start", NodeType.START.getCode()),
            specialNode("br", NodeType.BRANCH.getCode()), node("a"), node("b"),
            specialNode("end", NodeType.END.getCode())));
        def.setEdges(List.of(edge("start", "br"), edge("br", "a"), conditionEdge("br", "b"),
            edge("a", "end"), edge("b", "end")));
        List<String> errors = validator.validate(def, registry("h-a", "h-b"));
        assertEquals(1, errors.size());
        assertTrue(errors.get(0).contains("分支节点的出边必须为条件分支"));
    }

    @Test
    void shouldPassBranchWithAllConditionEdges() {
        DagDefModel def = new DagDefModel();
        def.setDagCode("d1");
        def.setDagVersion(1);
        def.setNodes(List.of(specialNode("start", NodeType.START.getCode()),
            specialNode("br", NodeType.BRANCH.getCode()), node("a"), node("b"),
            specialNode("end", NodeType.END.getCode())));
        def.setEdges(List.of(edge("start", "br"), conditionEdge("br", "a"), conditionEdge("br", "b"),
            edge("a", "end"), edge("b", "end")));
        assertTrue(validator.validate(def, registry("h-a", "h-b")).isEmpty());
    }

    @Test
    void shouldRejectIllegalNodeType() {
        DagDefModel def = new DagDefModel();
        def.setDagCode("d1");
        def.setDagVersion(1);
        NodeDef n = node("a");
        n.setNodeType("FOO");
        def.setNodes(List.of(specialNode("start", NodeType.START.getCode()), n,
            specialNode("end", NodeType.END.getCode())));
        def.setEdges(List.of(edge("start", "a"), edge("a", "end")));
        List<String> errors = validator.validate(def, registry("h-a"));
        assertEquals(1, errors.size());
        assertTrue(errors.get(0).contains("nodeType 非法"));
    }

    // ===== 第 8 项：nodeType 与 handler 匹配校验 =====

    @Test
    void shouldPassStartWithSystemStartHandler() {
        DagDefModel def = new DagDefModel();
        def.setDagCode("d1");
        def.setDagVersion(1);
        NodeDef start = specialNode("start", NodeType.START.getCode());
        start.setHandler(SystemHandler.START.getCode());
        def.setNodes(List.of(start, node("a"), specialNode("end", NodeType.END.getCode())));
        def.setEdges(List.of(edge("start", "a"), edge("a", "end")));
        assertTrue(validator.validate(def, registry("h-a", SystemHandler.START.getCode())).isEmpty());
    }

    @Test
    void shouldRejectStartWithEndHandler() {
        DagDefModel def = new DagDefModel();
        def.setDagCode("d1");
        def.setDagVersion(1);
        NodeDef start = specialNode("start", NodeType.START.getCode());
        start.setHandler(SystemHandler.END.getCode());
        def.setNodes(List.of(start, node("a"), specialNode("end", NodeType.END.getCode())));
        def.setEdges(List.of(edge("start", "a"), edge("a", "end")));
        List<String> errors = validator.validate(def, registry("h-a", SystemHandler.END.getCode()));
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
        def.setNodes(List.of(specialNode("start", NodeType.START.getCode()), br, node("a"), node("b"),
            specialNode("end", NodeType.END.getCode())));
        def.setEdges(List.of(edge("start", "br"), conditionEdge("br", "a"), conditionEdge("br", "b"),
            edge("a", "end"), edge("b", "end")));
        assertTrue(validator.validate(def, registry("h-a", "h-b", SystemHandler.BRANCH.getCode())).isEmpty());
    }

    @Test
    void shouldPassNormalWithSystemTimerHandler() {
        DagDefModel def = new DagDefModel();
        def.setDagCode("d1");
        def.setDagVersion(1);
        NodeDef t = node("t");
        t.setHandler(SystemHandler.TIMER.getCode());
        def.setNodes(List.of(specialNode("start", NodeType.START.getCode()), t,
            specialNode("end", NodeType.END.getCode())));
        def.setEdges(List.of(edge("start", "t"), edge("t", "end")));
        assertTrue(validator.validate(def, registry(SystemHandler.TIMER.getCode())).isEmpty());
    }

    @Test
    void shouldRejectStartWithTimerHandler() {
        DagDefModel def = new DagDefModel();
        def.setDagCode("d1");
        def.setDagVersion(1);
        NodeDef start = specialNode("start", NodeType.START.getCode());
        start.setHandler(SystemHandler.TIMER.getCode());   // system-timer 仅限 NORMAL 节点
        def.setNodes(List.of(start, node("a"), specialNode("end", NodeType.END.getCode())));
        def.setEdges(List.of(edge("start", "a"), edge("a", "end")));
        List<String> errors = validator.validate(def, registry("h-a", SystemHandler.TIMER.getCode()));
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
        def.setNodes(List.of(specialNode("start", NodeType.START.getCode()), n,
            specialNode("end", NodeType.END.getCode())));
        def.setEdges(List.of(edge("start", "a"), edge("a", "end")));
        List<String> errors = validator.validate(def, registry(SystemHandler.START.getCode()));
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
        def.setNodes(List.of(start, node("a"), specialNode("end", NodeType.END.getCode())));
        def.setEdges(List.of(edge("start", "a"), edge("a", "end")));
        List<String> errors = validator.validate(def, registry("h-a"));
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
        def.setNodes(List.of(start, node("a"), specialNode("end", NodeType.END.getCode())));
        def.setEdges(List.of(edge("start", "a"), edge("a", "end")));
        List<String> errors = validator.validate(def, registry("h-a"));
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
        def.setNodes(List.of(start, node("a"), specialNode("end", NodeType.END.getCode())));
        def.setEdges(List.of(edge("start", "a"), edge("a", "end")));
        List<String> errors = validator.validate(def, registry("h-a"));
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
        def.setNodes(List.of(start, node("a"), specialNode("end", NodeType.END.getCode())));
        def.setEdges(List.of(edge("start", "a"), edge("a", "end")));
        assertTrue(validator.validate(def, registry("h-a")).isEmpty());
    }

    @Test
    void shouldIgnoreTimerCheckForNonStartNode() {
        DagDefModel def = new DagDefModel();
        def.setDagCode("d1");
        def.setDagVersion(1);
        NodeDef n = node("a");
        n.setParams(Map.of("triggerType", "TIMER", "cron", "* * * *"));   // 非 START 节点不做 TIMER cron 校验
        def.setNodes(List.of(specialNode("start", NodeType.START.getCode()), n,
            specialNode("end", NodeType.END.getCode())));
        def.setEdges(List.of(edge("start", "a"), edge("a", "end")));
        assertTrue(validator.validate(def, registry("h-a")).isEmpty());
    }

    // ===== 第 9 项：节点参数 schema 校验 =====

    /** 含系统 handler 的注册表 */
    private NodeHandlerRegistry systemRegistry() {
        NodeHandlerRegistry registry = new NodeHandlerRegistry();
        ReflectionTestUtils.setField(registry, "handlerList",
            List.of(new SystemStartHandler(), new SystemTimerHandler(), handlerOf("h-a")));
        registry.init();
        return registry;
    }

    @Test
    void shouldRejectMissingCronWhenTriggerTypeTimer() {
        DagDefModel def = new DagDefModel();
        def.setDagCode("d1");
        def.setDagVersion(1);
        NodeDef start = specialNode("start", NodeType.START.getCode());
        start.setHandler(SystemHandler.START.getCode());
        start.setParams(Map.of("triggerType", "TIMER"));
        def.setNodes(List.of(start, node("a"), specialNode("end", NodeType.END.getCode())));
        def.setEdges(List.of(edge("start", "a"), edge("a", "end")));
        List<String> errors = validator.validate(def, systemRegistry());
        assertEquals(1, errors.size());
        assertTrue(errors.get(0).contains("必填"));
    }

    @Test
    void shouldRejectBadCronViaSchema() {
        DagDefModel def = new DagDefModel();
        def.setDagCode("d1");
        def.setDagVersion(1);
        NodeDef start = specialNode("start", NodeType.START.getCode());
        start.setHandler(SystemHandler.START.getCode());
        start.setParams(Map.of("triggerType", "TIMER", "cron", "* * * *"));   // cron 域数不足
        def.setNodes(List.of(start, node("a"), specialNode("end", NodeType.END.getCode())));
        def.setEdges(List.of(edge("start", "a"), edge("a", "end")));
        List<String> errors = validator.validate(def, systemRegistry());
        assertEquals(1, errors.size());
        assertTrue(errors.get(0).contains("非法"));
    }

    @Test
    void shouldRejectDelayMsOutOfRange() {
        DagDefModel def = new DagDefModel();
        def.setDagCode("d1");
        def.setDagVersion(1);
        NodeDef t = node("t");
        t.setHandler(SystemHandler.TIMER.getCode());
        t.setParams(Map.of("delayMs", 90000000));   // 超出 [0, 86400000]
        def.setNodes(List.of(specialNode("start", NodeType.START.getCode()), t,
            specialNode("end", NodeType.END.getCode())));
        def.setEdges(List.of(edge("start", "t"), edge("t", "end")));
        List<String> errors = validator.validate(def, systemRegistry());
        assertEquals(1, errors.size());
        assertTrue(errors.get(0).contains("超出范围"));
    }

    @Test
    void shouldRejectBadDelayUntil() {
        DagDefModel def = new DagDefModel();
        def.setDagCode("d1");
        def.setDagVersion(1);
        NodeDef t = node("t");
        t.setHandler(SystemHandler.TIMER.getCode());
        t.setParams(Map.of("delayUntil", "not-a-time"));
        def.setNodes(List.of(specialNode("start", NodeType.START.getCode()), t,
            specialNode("end", NodeType.END.getCode())));
        def.setEdges(List.of(edge("start", "t"), edge("t", "end")));
        List<String> errors = validator.validate(def, systemRegistry());
        assertEquals(1, errors.size());
        assertTrue(errors.get(0).contains("非法"));
    }

    @Test
    void shouldRejectNonMapTriggerParams() {
        DagDefModel def = new DagDefModel();
        def.setDagCode("d1");
        def.setDagVersion(1);
        NodeDef start = specialNode("start", NodeType.START.getCode());
        start.setHandler(SystemHandler.START.getCode());
        start.setParams(Map.of("triggerParams", "x"));   // KEY_VALUE 类型须为对象结构
        def.setNodes(List.of(start, node("a"), specialNode("end", NodeType.END.getCode())));
        def.setEdges(List.of(edge("start", "a"), edge("a", "end")));
        List<String> errors = validator.validate(def, systemRegistry());
        assertEquals(1, errors.size());
        assertTrue(errors.get(0).contains("对象结构"));
    }

    @Test
    void shouldRejectIllegalSelectValue() {
        DagDefModel def = new DagDefModel();
        def.setDagCode("d1");
        def.setDagVersion(1);
        NodeDef start = specialNode("start", NodeType.START.getCode());
        start.setHandler(SystemHandler.START.getCode());
        start.setParams(Map.of("triggerType", "XXX"));   // 不在 SELECT 选项中
        def.setNodes(List.of(start, node("a"), specialNode("end", NodeType.END.getCode())));
        def.setEdges(List.of(edge("start", "a"), edge("a", "end")));
        List<String> errors = validator.validate(def, systemRegistry());
        assertEquals(1, errors.size());
        assertTrue(errors.get(0).contains("取值非法"));
    }

    @Test
    void shouldPassFreeParamsNotInSchema() {
        DagDefModel def = new DagDefModel();
        def.setDagCode("d1");
        def.setDagVersion(1);
        NodeDef t = node("t");
        t.setHandler(SystemHandler.TIMER.getCode());
        t.setParams(Map.of("customKey", "自由参数", "delayMs", 500));   // schema 未声明的自由键不拦截
        def.setNodes(List.of(specialNode("start", NodeType.START.getCode()), t,
            specialNode("end", NodeType.END.getCode())));
        def.setEdges(List.of(edge("start", "t"), edge("t", "end")));
        List<String> errors = validator.validate(def, systemRegistry());
        assertTrue(errors.isEmpty());
    }
}
