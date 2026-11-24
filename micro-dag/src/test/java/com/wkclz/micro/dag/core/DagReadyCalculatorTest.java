package com.wkclz.micro.dag.core;

import com.wkclz.micro.dag.bean.constant.DagConstants;
import com.wkclz.micro.dag.bean.dto.DagDefModel;
import com.wkclz.micro.dag.bean.dto.EdgeDef;
import com.wkclz.micro.dag.bean.dto.NodeDef;
import com.wkclz.micro.dag.bean.enums.NodeStatus;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 就绪计算单测：上游满足才就绪 / SKIPPED 视为满足 / 条件边求值 / 分支级联跳过（失败下游保持等待） / 实例终态
 */
class DagReadyCalculatorTest {

    private final DagReadyCalculator calc = buildCalculator();

    /** @Resource 字段注入的手工装配 */
    private DagReadyCalculator buildCalculator() {
        DagReadyCalculator calc = new DagReadyCalculator();
        ReflectionTestUtils.setField(calc, "evaluator", new DagExpressionEvaluator());
        return calc;
    }

    /** 构建 a -> (b) 且 a ->(条件)-> c 的定义 */
    private DagDefModel defWithCondition(String when) {
        NodeDef a = new NodeDef(); a.setCode("a"); a.setHandler("h-a");
        NodeDef b = new NodeDef(); b.setCode("b"); b.setHandler("h-b");
        NodeDef c = new NodeDef(); c.setCode("c"); c.setHandler("h-c");
        EdgeDef ab = new EdgeDef(); ab.setFromCode("a"); ab.setToCode("b");
        EdgeDef ac = new EdgeDef(); ac.setFromCode("a"); ac.setToCode("c"); ac.setType(DagConstants.EDGE_TYPE_CONDITION); ac.setWhenExpr(when);
        DagDefModel def = new DagDefModel();
        def.setNodes(List.of(a, b, c));
        def.setEdges(List.of(ab, ac));
        return def;
    }

    private Map<String, NodeStatus> status(String node, NodeStatus s) {
        Map<String, NodeStatus> map = new HashMap<>();
        map.put(node, s);
        return map;
    }

    @Test
    void shouldReturnStartNodesWhenNoUpstream() {
        DagDefModel def = defWithCondition("${ctx.t} == 'x'");
        Set<String> ready = calc.readyNodes(def, status("a", NodeStatus.PENDING), Map.of(), Map.of());
        assertEquals(Set.of("a"), ready);
    }

    @Test
    void shouldNotReadyUntilUpstreamSatisfied() {
        DagDefModel def = defWithCondition("${ctx.t} == 'x'");
        Map<String, NodeStatus> all = new HashMap<>();
        all.put("a", NodeStatus.RUNNING);
        all.put("b", NodeStatus.PENDING);
        all.put("c", NodeStatus.PENDING);
        assertTrue(calc.readyNodes(def, all, Map.of(), Map.of()).isEmpty());
    }

    @Test
    void shouldTreatSkippedAsSatisfied() {
        DagDefModel def = defWithCondition("${ctx.t} == 'x'");
        Map<String, NodeStatus> all = new HashMap<>();
        all.put("a", NodeStatus.SKIPPED);
        all.put("b", NodeStatus.PENDING);
        Set<String> ready = calc.readyNodes(def, all, Map.of(), Map.of());
        // a 被 SKIPPED，条件边视为不满足，b 普通边满足
        assertEquals(Set.of("b"), ready);
    }

    @Test
    void shouldEvaluateConditionEdge() {
        DagDefModel def = defWithCondition("${ctx.t} == 'api'");
        Map<String, NodeStatus> all = new HashMap<>();
        all.put("a", NodeStatus.SUCCESS);
        all.put("b", NodeStatus.PENDING);
        all.put("c", NodeStatus.PENDING);
        Map<String, Object> ctx = Map.of("t", "api");
        Set<String> ready = calc.readyNodes(def, all, ctx, Map.of());
        assertEquals(Set.of("b", "c"), ready);
        // 条件不满足时 c 不就绪
        Set<String> ready2 = calc.readyNodes(def, all, Map.of("t", "file"), Map.of());
        assertEquals(Set.of("b"), ready2);
    }

    @Test
    void shouldCascadeSkipWhenAllIncomingUnsatisfied() {
        DagDefModel def = defWithCondition("${ctx.t} == 'api'");
        Map<String, NodeStatus> all = new HashMap<>();
        all.put("a", NodeStatus.SUCCESS);
        all.put("b", NodeStatus.PENDING);
        all.put("c", NodeStatus.PENDING);
        Map<String, Object> ctx = Map.of("t", "file");
        // c 的唯一入边条件不满足 → c 级联 SKIPPED
        assertEquals(Set.of("c"), calc.cascadeSkips(def, all, ctx, Map.of()));
    }

    @Test
    void shouldSkipDownstreamOfSkipped() {
        // a SUCCESS 但条件边不满足 → c SKIPPED → c 的下游 d SKIPPED
        NodeDef a = new NodeDef(); a.setCode("a"); a.setHandler("h-a");
        NodeDef c = new NodeDef(); c.setCode("c"); c.setHandler("h-c");
        NodeDef d = new NodeDef(); d.setCode("d"); d.setHandler("h-d");
        EdgeDef ac = new EdgeDef(); ac.setFromCode("a"); ac.setToCode("c"); ac.setType(DagConstants.EDGE_TYPE_CONDITION); ac.setWhenExpr("${ctx.t} == 'api'");
        EdgeDef cd = new EdgeDef(); cd.setFromCode("c"); cd.setToCode("d");
        DagDefModel def = new DagDefModel();
        def.setNodes(List.of(a, c, d));
        def.setEdges(List.of(ac, cd));
        Map<String, NodeStatus> all = new HashMap<>();
        all.put("a", NodeStatus.SUCCESS);
        all.put("c", NodeStatus.PENDING);
        all.put("d", NodeStatus.PENDING);
        Set<String> skips = calc.cascadeSkips(def, all, Map.of("t", "file"), Map.of());
        assertEquals(Set.of("c", "d"), skips);
    }

    @Test
    void shouldNotSkipWhenUpstreamUndetermined() {
        // a -> b -> c 全普通边：a SUCCESS、b 尚 PENDING，c 的上游未定 → 不得级联跳过
        NodeDef a = new NodeDef(); a.setCode("a"); a.setHandler("h-a");
        NodeDef b = new NodeDef(); b.setCode("b"); b.setHandler("h-b");
        NodeDef c = new NodeDef(); c.setCode("c"); c.setHandler("h-c");
        EdgeDef ab = new EdgeDef(); ab.setFromCode("a"); ab.setToCode("b");
        EdgeDef bc = new EdgeDef(); bc.setFromCode("b"); bc.setToCode("c");
        DagDefModel def = new DagDefModel();
        def.setNodes(List.of(a, b, c));
        def.setEdges(List.of(ab, bc));
        Map<String, NodeStatus> all = new HashMap<>();
        all.put("a", NodeStatus.SUCCESS);
        all.put("b", NodeStatus.PENDING);
        all.put("c", NodeStatus.PENDING);
        assertEquals(Set.of(), calc.cascadeSkips(def, all, Map.of(), Map.of()));
    }

    @Test
    void shouldNotSkipWhenUpstreamFailed() {
        // a FAILED → b/c 保持 PENDING 等待，不得级联跳过；MANUAL_STOPPED 同语义
        NodeDef a = new NodeDef(); a.setCode("a"); a.setHandler("h-a");
        NodeDef b = new NodeDef(); b.setCode("b"); b.setHandler("h-b");
        NodeDef c = new NodeDef(); c.setCode("c"); c.setHandler("h-c");
        EdgeDef ab = new EdgeDef(); ab.setFromCode("a"); ab.setToCode("b");
        EdgeDef bc = new EdgeDef(); bc.setFromCode("b"); bc.setToCode("c");
        DagDefModel def = new DagDefModel();
        def.setNodes(List.of(a, b, c));
        def.setEdges(List.of(ab, bc));
        Map<String, NodeStatus> all = new HashMap<>();
        all.put("a", NodeStatus.FAILED);
        all.put("b", NodeStatus.PENDING);
        all.put("c", NodeStatus.PENDING);
        assertEquals(Set.of(), calc.cascadeSkips(def, all, Map.of(), Map.of()));
        all.put("a", NodeStatus.MANUAL_STOPPED);
        assertEquals(Set.of(), calc.cascadeSkips(def, all, Map.of(), Map.of()));
    }

    @Test
    void shouldComputeInstanceTerminal() {
        Map<String, NodeStatus> all = new HashMap<>();
        all.put("a", NodeStatus.SUCCESS);
        all.put("b", NodeStatus.SKIPPED);
        assertEquals("SUCCESS", calc.instanceTerminalStatus(all).orElse(null));
        // b FAILED、下游 c PENDING 等待 → 实例 FAILED
        all.put("b", NodeStatus.FAILED);
        all.put("c", NodeStatus.PENDING);
        assertEquals("FAILED", calc.instanceTerminalStatus(all).orElse(null));
        // 无失败但仍有 PENDING → 未终态
        all.put("b", NodeStatus.SKIPPED);
        assertTrue(calc.instanceTerminalStatus(all).isEmpty());
        // 存在 FAILED 且有在途节点 → 未终态
        all.put("b", NodeStatus.FAILED);
        all.put("c", NodeStatus.RUNNING);
        assertTrue(calc.instanceTerminalStatus(all).isEmpty());
    }
}
