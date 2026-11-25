package com.wkclz.micro.dag.handler.system;

import com.wkclz.core.exception.ValidationException;
import com.wkclz.micro.dag.api.NodeContext;
import com.wkclz.micro.dag.api.NodeResult;
import com.wkclz.micro.dag.bean.dto.NodeParamDef;
import com.wkclz.micro.dag.bean.entity.DagNodeInstance;
import com.wkclz.micro.dag.bean.enums.NodeCategory;
import com.wkclz.micro.dag.bean.enums.NodeParamType;
import com.wkclz.micro.dag.bean.enums.NodeStatus;
import com.wkclz.micro.dag.bean.enums.SystemHandler;
import com.wkclz.micro.dag.core.DagExpressionEvaluator;
import com.wkclz.micro.dag.mapper.DagNodeInstanceMapper;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * 系统 handler 单元测试（system-start/end/branch/timer）
 */
class SystemHandlersTest {

    private NodeContext ctx(String inputJson, AtomicBoolean interrupted) {
        return new NodeContext("INS-1", "node-1", "TRC-1", 1, inputJson, interrupted == null ? () -> false : interrupted::get);
    }

    /** @Resource 字段注入的手工装配 */
    private SystemBranchHandler branchHandler() {
        SystemBranchHandler handler = new SystemBranchHandler();
        ReflectionTestUtils.setField(handler, "evaluator", new DagExpressionEvaluator());
        return handler;
    }

    /** system-timer 手工装配（mapper 可为 null：无 upstreams 参数时不触发 Mapper 调用） */
    private SystemTimerHandler timerHandler(DagNodeInstanceMapper mapper) {
        SystemTimerHandler handler = new SystemTimerHandler();
        if (mapper != null) {
            ReflectionTestUtils.setField(handler, "nodeInstanceMapper", mapper);
        }
        return handler;
    }

    private DagNodeInstance node(String code, String status) {
        DagNodeInstance n = new DagNodeInstance();
        n.setNodeCode(code);
        n.setStatus(status);
        return n;
    }

    // ---------- system-start ----------

    @Test
    void startPassesThroughParams() {
        SystemStartHandler handler = new SystemStartHandler();
        assertEquals(SystemHandler.START.getCode(), handler.nodeCode());
        assertEquals("系统-开始节点", handler.nodeName());
        assertEquals(NodeCategory.SYSTEM, handler.category());
        NodeResult result = handler.execute(ctx("{\"a\":1,\"b\":\"x\"}", null));
        assertEquals(Map.of("a", 1, "b", "x"), result.getOutput());
    }

    // ---------- system-end ----------

    @Test
    void endSucceedsWithEmptyOutput() {
        SystemEndHandler handler = new SystemEndHandler();
        assertEquals(SystemHandler.END.getCode(), handler.nodeCode());
        assertEquals("系统-结束节点", handler.nodeName());
        assertEquals(NodeCategory.SYSTEM, handler.category());
        NodeResult result = handler.execute(ctx("{\"a\":1}", null));
        assertEquals(Map.of(), result.getOutput());
    }

    // ---------- system-branch ----------

    @Test
    void branchEvaluatesConditions() {
        SystemBranchHandler handler = branchHandler();
        assertEquals(SystemHandler.BRANCH.getCode(), handler.nodeCode());
        assertEquals("系统-分支节点", handler.nodeName());
        assertEquals(NodeCategory.SYSTEM, handler.category());
        String json = "{\"amount\":120,\"branches\":{\"node-a\":\"${ctx.amount} > 100\",\"node-b\":\"${ctx.amount} > 200\"}}";
        NodeResult result = handler.execute(ctx(json, null));
        Map<String, Object> output = result.getOutput();
        @SuppressWarnings("unchecked")
        Map<String, Object> branches = (Map<String, Object>) output.get("branches");
        assertEquals(true, branches.get("node-a"));
        assertEquals(false, branches.get("node-b"));
        assertEquals(List.of("node-a"), output.get("selected"));
    }

    @Test
    void branchMissingBranchesReturnsEmpty() {
        SystemBranchHandler handler = branchHandler();
        NodeResult result = handler.execute(ctx("{\"amount\":1}", null));
        assertEquals(Map.of("branches", Map.of(), "selected", List.of()), result.getOutput());
        // branches 非 Map 同样返回空明细
        NodeResult result2 = handler.execute(ctx("{\"branches\":\"x\"}", null));
        assertEquals(Map.of("branches", Map.of(), "selected", List.of()), result2.getOutput());
    }

    // ---------- system-timer ----------

    @Test
    void timerDelaysConfiguredMs() throws Exception {
        SystemTimerHandler handler = new SystemTimerHandler();
        assertEquals(SystemHandler.TIMER.getCode(), handler.nodeCode());
        assertEquals("系统-定时器", handler.nodeName());
        assertEquals(NodeCategory.SYSTEM, handler.category());
        long start = System.currentTimeMillis();
        NodeResult result = handler.execute(ctx("{\"delayMs\":250}", null));
        assertTrue(System.currentTimeMillis() - start >= 250);
        assertTrue((Long) result.getOutput().get("waitedMs") >= 250);
        assertNotNull(result.getOutput().get("targetTime"));
    }

    @Test
    void timerNoParamsSucceedsImmediately() throws Exception {
        SystemTimerHandler handler = new SystemTimerHandler();
        NodeResult result = handler.execute(ctx(null, null));
        assertEquals(0L, result.getOutput().get("waitedMs"));
    }

    @Test
    void timerDelayUntilPastSucceedsImmediately() throws Exception {
        SystemTimerHandler handler = new SystemTimerHandler();
        String past = LocalDateTime.now().minusHours(1).toString();
        NodeResult result = handler.execute(ctx("{\"delayUntil\":\"" + past + "\"}", null));
        assertEquals(0L, result.getOutput().get("waitedMs"));
    }

    @Test
    void timerInvalidDelayUntilRejected() {
        SystemTimerHandler handler = new SystemTimerHandler();
        assertThrows(ValidationException.class, () -> handler.execute(ctx("{\"delayUntil\":\"not-a-time\"}", null)));
    }

    @Test
    void timerUpstreamSkippedCountsAsDone() throws Exception {
        DagNodeInstanceMapper mapper = mock(DagNodeInstanceMapper.class);
        when(mapper.listByInstanceCode("INS-1")).thenReturn(List.of(node("u1", NodeStatus.SKIPPED.name())));
        String past = LocalDateTime.now().minusHours(2).toString();
        NodeResult result = timerHandler(mapper)
            .execute(ctx("{\"upstreams\":[\"u1\"],\"delayUntil\":\"" + past + "\"}", null));
        // 上游 SKIPPED 视为完成，到点时间 2h 前在 24h 内 → 立即成功
        assertEquals(0L, result.getOutput().get("waitedMs"));
    }

    @Test
    void timerUpstreamNotDoneFails() throws Exception {
        DagNodeInstanceMapper mapper = mock(DagNodeInstanceMapper.class);
        when(mapper.listByInstanceCode("INS-1")).thenReturn(List.of(
            node("nodeA", NodeStatus.RUNNING.name()), node("nodeB", NodeStatus.FAILED.name())));
        NodeResult result = timerHandler(mapper)
            .execute(ctx("{\"upstreams\":[\"nodeA\",\"nodeB\"]}", null));
        assertFalse(result.isSuccess());
        String msg = String.valueOf(result.getOutput().get("msg"));
        assertTrue(msg.contains("nodeA=RUNNING"));
        assertTrue(msg.contains("nodeB=FAILED"));
    }

    @Test
    void timerExpiredTargetFails() throws Exception {
        String expired = LocalDateTime.now().minusDays(2).toString();
        NodeResult result = timerHandler(null)
            .execute(ctx("{\"delayUntil\":\"" + expired + "\"}", null));
        assertFalse(result.isSuccess());
        assertTrue(String.valueOf(result.getOutput().get("msg")).contains("定时任务已过期超过 1 天"));
    }

    @Test
    void timerUpstreamsMissingBehavesAsBefore() throws Exception {
        // upstreams 缺失或为空 → 与现状一致，走正常等待路径
        long start = System.currentTimeMillis();
        NodeResult result = timerHandler(null).execute(ctx("{\"delayMs\":50}", null));
        assertTrue(System.currentTimeMillis() - start >= 50);
        assertTrue((Long) result.getOutput().get("waitedMs") >= 50);
        NodeResult result2 = timerHandler(null).execute(ctx("{\"upstreams\":[],\"delayMs\":50}", null));
        assertTrue((Long) result2.getOutput().get("waitedMs") >= 50);
    }

    // ---------- 参数 schema 声明 ----------

    @Test
    void startParamSchemaDeclared() {
        SystemStartHandler handler = new SystemStartHandler();
        List<NodeParamDef> schema = handler.paramSchema();
        assertEquals(3, schema.size());
        NodeParamDef triggerType = schema.get(0);
        assertEquals("triggerType", triggerType.getKey());
        assertEquals(NodeParamType.SELECT, triggerType.getType());
        assertEquals(2, triggerType.getOptions().size());
        assertEquals("MANUAL", triggerType.getDefaultValue());
        NodeParamDef cron = schema.get(1);
        assertEquals("cron", cron.getKey());
        assertEquals("triggerType", cron.getRequiredWhenKey());
        assertEquals("TIMER", cron.getRequiredWhenValue());
        NodeParamDef triggerParams = schema.get(2);
        assertEquals(NodeParamType.KEY_VALUE, triggerParams.getType());
    }

    @Test
    void timerParamSchemaDeclared() {
        SystemTimerHandler handler = new SystemTimerHandler();
        List<NodeParamDef> schema = handler.paramSchema();
        assertEquals(2, schema.size());
        NodeParamDef delayMs = schema.get(0);
        assertEquals("delayMs", delayMs.getKey());
        assertEquals(0L, delayMs.getMin());
        assertEquals(86400000L, delayMs.getMax());
        assertEquals(NodeParamType.DATETIME, schema.get(1).getType());
    }
}
