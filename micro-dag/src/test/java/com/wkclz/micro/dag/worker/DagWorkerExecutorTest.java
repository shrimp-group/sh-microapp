package com.wkclz.micro.dag.worker;

import com.wkclz.micro.dag.api.NodeContext;
import com.wkclz.micro.dag.api.NodeHandler;
import com.wkclz.micro.dag.api.NodeHandlerRegistry;
import com.wkclz.micro.dag.api.NodeResult;
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
import org.mockito.InOrder;
import org.mockito.Mockito;

import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
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
        executor = new DagWorkerExecutor(registry, nodeMapper, engine, logService, publisher, new DagProperties());
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
            eq(ExecutionEvent.TIMEOUT), contains("300"));
    }

    @Test
    void submit_cleanupHookRunsBeforeExecute() throws Exception {
        when(handler.execute(any())).thenReturn(NodeResult.ok());

        executor.submit(node(null, 1), instance());

        InOrder inOrder = inOrder(handler);
        inOrder.verify(handler, timeout(VERIFY_TIMEOUT_MS)).cleanup(any(NodeContext.class));
        inOrder.verify(handler).execute(any(NodeContext.class));
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
            eq(ExecutionEvent.FAIL), contains("boom"));
        Mockito.verify(publisher, timeout(VERIFY_TIMEOUT_MS)).wakeUp();
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
        executor = new DagWorkerExecutor(registry, nodeMapper, engine, logService, publisher, properties);
        when(handler.execute(any())).thenAnswer(inv -> {
            Thread.sleep(200);
            return NodeResult.ok();
        });
        when(nodeMapper.renewLease(any(), anyString(), any())).thenReturn(1);

        executor.submit(node(null, 0), instance());

        verify(nodeMapper, timeout(VERIFY_TIMEOUT_MS).atLeastOnce()).renewLease(eq(1L), anyString(), any());
        verify(engine, timeout(VERIFY_TIMEOUT_MS)).onSuccess(eq(INSTANCE_CODE), eq(NODE_CODE), any());
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
