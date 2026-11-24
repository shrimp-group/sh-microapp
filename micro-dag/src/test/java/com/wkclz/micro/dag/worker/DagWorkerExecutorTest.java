package com.wkclz.micro.dag.worker;

import com.wkclz.micro.dag.api.NodeContext;
import com.wkclz.micro.dag.api.NodeHandler;
import com.wkclz.micro.dag.api.NodeHandlerRegistry;
import com.wkclz.micro.dag.api.NodeResult;
import com.wkclz.micro.dag.bean.dto.NodeInstanceLeaseDto;
import com.wkclz.micro.dag.bean.entity.DagInstance;
import com.wkclz.micro.dag.bean.entity.DagNodeInstance;
import com.wkclz.micro.dag.bean.enums.ExecutionEvent;
import com.wkclz.micro.dag.cache.DagRedisPublisher;
import com.wkclz.micro.dag.config.DagProperties;
import com.wkclz.micro.dag.mapper.DagNodeInstanceMapper;
import com.wkclz.micro.dag.service.DagEngineService;
import com.wkclz.micro.dag.service.DagExecutionLogService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.Mockito;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class DagWorkerExecutorTest {

    private static final String INSTANCE_CODE = "INST20261114000001";
    private static final String NODE_CODE = "N_pay";
    private static final long VERIFY_TIMEOUT_MS = 5000;

    private NodeHandlerRegistry registry;
    private DagNodeInstanceMapper nodeMapper;
    private DagEngineService engine;
    private DagExecutionLogService logService;
    private DagRedisPublisher publisher;
    private NodeHandler handler;
    private DagWorkerExecutor executor;

    @BeforeEach
    void setUp() {
        registry = mock(NodeHandlerRegistry.class);
        nodeMapper = mock(DagNodeInstanceMapper.class);
        engine = mock(DagEngineService.class);
        logService = mock(DagExecutionLogService.class);
        publisher = mock(DagRedisPublisher.class);
        handler = mock(NodeHandler.class);
        when(registry.get(anyString())).thenReturn(handler);
        executor = buildExecutor(new DagProperties());
    }

    /** @Resource 字段注入的手工装配：new 后 setField 并触发 @PostConstruct 初始化线程池 */
    private DagWorkerExecutor buildExecutor(DagProperties properties) {
        DagWorkerExecutor executor = new DagWorkerExecutor();
        ReflectionTestUtils.setField(executor, "registry", registry);
        ReflectionTestUtils.setField(executor, "nodeMapper", nodeMapper);
        ReflectionTestUtils.setField(executor, "engine", engine);
        ReflectionTestUtils.setField(executor, "logService", logService);
        ReflectionTestUtils.setField(executor, "publisher", publisher);
        ReflectionTestUtils.setField(executor, "properties", properties);
        executor.init();
        return executor;
    }

    @Test
    void submit_success_onSuccessDispatched() throws Exception {
        when(handler.execute(any())).thenReturn(NodeResult.ok(Map.of("k", "v")));

        executor.submit(node(null, 0), instance());

        verify(engine, timeout(VERIFY_TIMEOUT_MS)).onSuccess(eq(INSTANCE_CODE), eq(NODE_CODE), eq(Map.of("k", "v")));
        awaitRemoved();
    }

    @Test
    void submit_success_traceCodeBackfilled() throws Exception {
        when(handler.execute(any())).thenReturn(NodeResult.ok().withTraceCode("TRC20261114001"));
        when(nodeMapper.backfillTraceCode(anyString(), anyString())).thenReturn(1);

        executor.submit(node(null, 0), instance());

        verify(nodeMapper, timeout(VERIFY_TIMEOUT_MS)).backfillTraceCode(INSTANCE_CODE, "TRC20261114001");
        verify(engine, timeout(VERIFY_TIMEOUT_MS)).onSuccess(eq(INSTANCE_CODE), eq(NODE_CODE), any());
    }

    @Test
    void submit_timeout_onFailureTimeoutDispatched() throws Exception {
        when(handler.execute(any())).thenAnswer(inv -> {
            Thread.sleep(3000);
            return NodeResult.ok();
        });

        executor.submit(node(300L, 0), instance());

        verify(engine, timeout(VERIFY_TIMEOUT_MS)).onFailure(eq(INSTANCE_CODE), eq(NODE_CODE),
            eq(ExecutionEvent.TIMEOUT), contains("300"), isNull());
    }

    @Test
    void submit_timeoutZero_treatedAsUnlimited_onSuccessDispatched() throws Exception {
        // timeout_ms=0 语义为"不限时"（与实体注释一致），不应触发 0ms 立即超时
        when(handler.execute(any())).thenAnswer(inv -> {
            Thread.sleep(300);
            return NodeResult.ok();
        });

        executor.submit(node(0L, 0), instance());

        verify(engine, timeout(VERIFY_TIMEOUT_MS)).onSuccess(eq(INSTANCE_CODE), eq(NODE_CODE), any());
        verify(engine, never()).onFailure(anyString(), anyString(), any(), anyString(), any());
    }

    @Test
    void submit_callbackThrows_exceptionSwallowedAndExecutorSurvives() throws Exception {
        // 回调链路（含事务提交）抛出的未分类异常必须兜底记录，且不影响执行器继续工作
        Mockito.doThrow(new RuntimeException("commit fail")).doNothing()
            .when(engine).onSuccess(anyString(), anyString(), any());
        when(handler.execute(any())).thenReturn(NodeResult.ok());

        executor.submit(node(null, 0), instance());

        // finally 唤醒 + 任务登记清理照常执行
        Mockito.verify(publisher, timeout(VERIFY_TIMEOUT_MS)).wakeUp();
        awaitRemoved();

        // 执行器仍可接续提交新任务并正常回调
        executor.submit(node(null, 0), instance());
        Mockito.verify(engine, timeout(VERIFY_TIMEOUT_MS).times(2)).onSuccess(eq(INSTANCE_CODE), eq(NODE_CODE), any());
    }

    @Test
    void submit_cleanupHookRunsBeforeExecute() throws Exception {
        when(handler.execute(any())).thenReturn(NodeResult.ok());

        executor.submit(node(null, 1), instance());

        InOrder inOrder = inOrder(handler);
        inOrder.verify(handler, timeout(VERIFY_TIMEOUT_MS)).cleanup(any(NodeContext.class));
        inOrder.verify(handler, timeout(VERIFY_TIMEOUT_MS)).execute(any(NodeContext.class));
        verify(logService, timeout(VERIFY_TIMEOUT_MS)).log(eq(INSTANCE_CODE), eq(NODE_CODE), eq(1),
            eq(ExecutionEvent.CLEANUP), isNull(), anyString());
        verify(nodeMapper, timeout(VERIFY_TIMEOUT_MS)).updateById(any());
        verify(engine, timeout(VERIFY_TIMEOUT_MS)).onSuccess(eq(INSTANCE_CODE), eq(NODE_CODE), any());
    }

    @Test
    void submit_handlerThrows_onFailureDispatched() throws Exception {
        when(handler.execute(any())).thenThrow(new RuntimeException("boom"));

        executor.submit(node(null, 0), instance());

        verify(engine, timeout(VERIFY_TIMEOUT_MS)).onFailure(eq(INSTANCE_CODE), eq(NODE_CODE),
            eq(ExecutionEvent.FAIL), contains("boom"), isNull());
        Mockito.verify(publisher, timeout(VERIFY_TIMEOUT_MS)).wakeUp();
    }

    @Test
    void submit_explicitFailResult_onFailureDispatchedWithOutput() throws Exception {
        // NodeResult.fail 显式失败：output.msg 作为失败消息，output 随事件派发
        when(handler.execute(any())).thenReturn(NodeResult.fail(Map.of("msg", "余额不足", "rows", 0)));

        executor.submit(node(null, 0), instance());

        verify(engine, timeout(VERIFY_TIMEOUT_MS)).onFailure(eq(INSTANCE_CODE), eq(NODE_CODE),
            eq(ExecutionEvent.FAIL), eq("余额不足"), eq(Map.of("msg", "余额不足", "rows", 0)));
        verify(engine, never()).onSuccess(anyString(), anyString(), any());
        awaitRemoved();
    }

    @Test
    void submit_explicitFailResultWithoutMsg_defaultFailMsg() throws Exception {
        // NodeResult.fail() 无 output：缺省失败文案
        when(handler.execute(any())).thenReturn(NodeResult.fail());

        executor.submit(node(null, 0), instance());

        verify(engine, timeout(VERIFY_TIMEOUT_MS)).onFailure(eq(INSTANCE_CODE), eq(NODE_CODE),
            eq(ExecutionEvent.FAIL), eq("handler 返回失败"), isNull());
        awaitRemoved();
    }

    @Test
    void submit_notReadyResult_onFailureDispatchedWithNotReady() throws Exception {
        // NodeResult.notReady 未就绪：派发 NOT_READY 事件（非 FAIL），output.msg 作为未就绪原因
        when(handler.execute(any())).thenReturn(NodeResult.notReady(Map.of("msg", "上游数据未落库")));

        executor.submit(node(null, 0), instance());

        verify(engine, timeout(VERIFY_TIMEOUT_MS)).onFailure(eq(INSTANCE_CODE), eq(NODE_CODE),
            eq(ExecutionEvent.NOT_READY), eq("上游数据未落库"), eq(Map.of("msg", "上游数据未落库")));
        verify(engine, never()).onSuccess(anyString(), anyString(), any());
        awaitRemoved();
    }

    @Test
    void submit_notReadyResultWithoutOutput_defaultNotReadyMsg() throws Exception {
        // NodeResult.notReady() 无 output：缺省未就绪文案
        when(handler.execute(any())).thenReturn(NodeResult.notReady());

        executor.submit(node(null, 0), instance());

        verify(engine, timeout(VERIFY_TIMEOUT_MS)).onFailure(eq(INSTANCE_CODE), eq(NODE_CODE),
            eq(ExecutionEvent.NOT_READY), eq("handler 返回未就绪"), isNull());
        awaitRemoved();
    }

    @Test
    void notReadyResult_stateFlags() {
        // notReady 语义：isSuccess() 保持 false（仅 ok 为 true），isNotReady() 为 true
        NodeResult result = NodeResult.notReady(Map.of("msg", "上游数据未落库"));
        assertFalse(result.isSuccess());
        assertTrue(result.isNotReady());
        assertTrue(NodeResult.notReady().isNotReady());
        assertFalse(NodeResult.ok().isNotReady());
        assertFalse(NodeResult.fail().isNotReady());
    }

    @Test
    void interrupt_runningTask_onInterruptedDispatched() throws Exception {
        // 标记位中断路径：interrupt 置标记并 cancel future，get 抛 CancellationException → onInterrupted
        CountDownLatch release = new CountDownLatch(1);
        when(handler.execute(any())).thenAnswer(inv -> {
            release.await(5, TimeUnit.SECONDS);
            return NodeResult.ok();
        });

        executor.submit(node(null, 0), instance());
        assertTrue(executor.isRunningHere(INSTANCE_CODE, NODE_CODE));
        executor.interrupt(INSTANCE_CODE, NODE_CODE);
        release.countDown();

        verify(engine, timeout(VERIFY_TIMEOUT_MS)).onInterrupted(INSTANCE_CODE, NODE_CODE);
        verify(engine, never()).onSuccess(anyString(), anyString(), any());
        awaitRemoved();
    }

    @Test
    void interrupt_cooperativeCheckInterrupted_onInterruptedDispatched() throws Exception {
        // 协作式中断路径：handler 调用 ctx.checkInterrupted() 抛 InterruptedException
        CountDownLatch started = new CountDownLatch(1);
        CountDownLatch release = new CountDownLatch(1);
        when(handler.execute(any())).thenAnswer(inv -> {
            started.countDown();
            release.await(5, TimeUnit.SECONDS);
            ((NodeContext) inv.getArgument(0)).checkInterrupted();
            return NodeResult.ok();
        });

        executor.submit(node(null, 0), instance());
        assertTrue(started.await(5, TimeUnit.SECONDS));
        executor.interrupt(INSTANCE_CODE, NODE_CODE);
        release.countDown();

        verify(engine, timeout(VERIFY_TIMEOUT_MS)).onInterrupted(INSTANCE_CODE, NODE_CODE);
        awaitRemoved();
    }

    @Test
    void isRunningHere_falseWhenNotSubmitted() {
        assertFalse(executor.isRunningHere(INSTANCE_CODE, NODE_CODE));
    }

    @Test
    void leaseRenewal_scheduledAndExecuted() throws Exception {
        DagProperties properties = new DagProperties();
        properties.getLease().setFixedMs(90);
        executor = buildExecutor(properties);
        when(handler.execute(any())).thenAnswer(inv -> {
            Thread.sleep(200);
            return NodeResult.ok();
        });
        when(nodeMapper.renewLease(any(NodeInstanceLeaseDto.class))).thenReturn(1);

        executor.submit(node(null, 0), instance());

        verify(nodeMapper, timeout(VERIFY_TIMEOUT_MS).atLeastOnce()).renewLease(any(NodeInstanceLeaseDto.class));
        verify(engine, timeout(VERIFY_TIMEOUT_MS)).onSuccess(eq(INSTANCE_CODE), eq(NODE_CODE), any());
        awaitRemoved();
    }

    @Test
    void submit_runningLogs_flushedPeriodically() throws Exception {
        // 运行中过程日志由 flusher 按固定间隔批量落库（NODE_LOG，无 detail_json）
        DagProperties properties = new DagProperties();
        properties.getLog().setFlushIntervalMs(100);
        executor = buildExecutor(properties);
        when(handler.execute(any())).thenAnswer(inv -> {
            NodeContext ctx = inv.getArgument(0);
            ctx.log("step-1");
            ctx.log("step-2");
            Thread.sleep(300);
            return NodeResult.ok();
        });

        executor.submit(node(null, 0), instance());

        verify(logService, timeout(VERIFY_TIMEOUT_MS)).log(eq(INSTANCE_CODE), eq(NODE_CODE), eq(1),
            eq(ExecutionEvent.NODE_LOG), isNull(), contains("step-1"), anyString());
        verify(logService, timeout(VERIFY_TIMEOUT_MS)).log(eq(INSTANCE_CODE), eq(NODE_CODE), eq(1),
            eq(ExecutionEvent.NODE_LOG), isNull(), contains("step-2"), anyString());
        awaitRemoved();
    }

    @Test
    void submit_emptyBuffer_flusherSkips() throws Exception {
        // 空缓冲跳过：flusher 多轮扫描但不产生任何 NODE_LOG 记录
        DagProperties properties = new DagProperties();
        properties.getLog().setFlushIntervalMs(100);
        executor = buildExecutor(properties);
        when(handler.execute(any())).thenAnswer(inv -> {
            Thread.sleep(300);
            return NodeResult.ok();
        });

        executor.submit(node(null, 0), instance());

        awaitRemoved();
        Mockito.verify(logService, never()).log(anyString(), anyString(), anyInt(),
            eq(ExecutionEvent.NODE_LOG), any(), any(), anyString());
    }

    @Test
    void submit_finalFlush_writesPendingLog() throws Exception {
        // 终态兜底 flush：flusher 间隔远大于执行时长，剩余缓冲由 supervise finally 兜底落库
        DagProperties properties = new DagProperties();
        properties.getLog().setFlushIntervalMs(60000);
        executor = buildExecutor(properties);
        when(handler.execute(any())).thenAnswer(inv -> {
            ((NodeContext) inv.getArgument(0)).log("final-line");
            return NodeResult.ok();
        });

        executor.submit(node(null, 0), instance());

        verify(logService, timeout(VERIFY_TIMEOUT_MS)).log(eq(INSTANCE_CODE), eq(NODE_CODE), eq(1),
            eq(ExecutionEvent.NODE_LOG), isNull(), eq("final-line"), anyString());
        awaitRemoved();
    }

    @Test
    void submit_neverReported_finalFlushWritesNothing() throws Exception {
        // 未上报不落库：handler 全程不 log，终态兜底也不写 NODE_LOG
        DagProperties properties = new DagProperties();
        properties.getLog().setFlushIntervalMs(60000);
        executor = buildExecutor(properties);
        when(handler.execute(any())).thenReturn(NodeResult.ok());

        executor.submit(node(null, 0), instance());

        awaitRemoved();
        Mockito.verify(logService, never()).log(anyString(), anyString(), anyInt(),
            eq(ExecutionEvent.NODE_LOG), any(), any(), anyString());
    }

    @Test
    void submit_logOverflow_finalFlushAppendsTruncatedMark() throws Exception {
        // 超限丢弃：缓冲内容不超上限，兜底 flush 追加截断标记
        DagProperties properties = new DagProperties();
        properties.getLog().setFlushIntervalMs(60000);
        properties.getLog().setMaxChars(50);
        executor = buildExecutor(properties);
        when(handler.execute(any())).thenAnswer(inv -> {
            NodeContext ctx = inv.getArgument(0);
            ctx.log("a".repeat(30));
            ctx.log("b".repeat(30)); // 累计 60 > 50：整条丢弃并置 truncated
            return NodeResult.ok();
        });

        executor.submit(node(null, 0), instance());

        ArgumentCaptor<String> content = ArgumentCaptor.forClass(String.class);
        verify(logService, timeout(VERIFY_TIMEOUT_MS)).log(eq(INSTANCE_CODE), eq(NODE_CODE), eq(1),
            eq(ExecutionEvent.NODE_LOG), isNull(), content.capture(), anyString());
        assertTrue(content.getValue().startsWith("a".repeat(30)));
        assertTrue(content.getValue().endsWith("...[truncated]"));
        assertTrue(content.getValue().length() <= 50 + "\n...[truncated]".length());
        awaitRemoved();
    }

    @Test
    void submit_flushInsertThrows_flusherSurvives() throws Exception {
        // flusher 落库失败不得停摆：scheduleAtFixedRate 任务抛异常会被永久取消，
        // 落库异常必须在 flush 内部消化，保证后续运行中日志继续按间隔落库（≥3 次 NODE_LOG 调用）
        DagProperties properties = new DagProperties();
        properties.getLog().setFlushIntervalMs(100);
        executor = buildExecutor(properties);
        Mockito.doThrow(new RuntimeException("db down")).when(logService).log(
            anyString(), anyString(), any(), any(), any(), any(), anyString());
        when(handler.execute(any())).thenAnswer(inv -> {
            NodeContext ctx = inv.getArgument(0);
            ctx.log("l1");
            Thread.sleep(400);
            ctx.log("l2");
            Thread.sleep(400);
            ctx.log("l3");
            Thread.sleep(400);
            return NodeResult.ok();
        });

        executor.submit(node(null, 0), instance());

        verify(logService, timeout(VERIFY_TIMEOUT_MS).atLeast(3)).log(eq(INSTANCE_CODE), eq(NODE_CODE), eq(1),
            eq(ExecutionEvent.NODE_LOG), isNull(), anyString(), anyString());
        awaitRemoved();
    }

    private DagNodeInstance node(Long timeoutMs, Integer cleanupFlag) {
        DagNodeInstance node = new DagNodeInstance();
        node.setId(1L);
        node.setInstanceCode(INSTANCE_CODE);
        node.setNodeCode(NODE_CODE);
        node.setHandler(NODE_CODE);
        node.setStatus("RUNNING");
        node.setAttempt(1);
        node.setTimeoutMs(timeoutMs);
        node.setCleanupFlag(cleanupFlag);
        return node;
    }

    private DagInstance instance() {
        DagInstance instance = new DagInstance();
        instance.setInstanceCode(INSTANCE_CODE);
        return instance;
    }

    /** 等待任务从登记表移除（finally 清理） */
    private void awaitRemoved() {
        long deadline = System.currentTimeMillis() + VERIFY_TIMEOUT_MS;
        while (executor.isRunningHere(INSTANCE_CODE, NODE_CODE) && System.currentTimeMillis() < deadline) {
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
        assertFalse(executor.isRunningHere(INSTANCE_CODE, NODE_CODE));
    }
}
