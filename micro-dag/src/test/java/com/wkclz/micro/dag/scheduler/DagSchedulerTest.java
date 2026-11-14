package com.wkclz.micro.dag.scheduler;

import com.wkclz.micro.dag.bean.dto.DagDefModel;
import com.wkclz.micro.dag.bean.entity.DagInstance;
import com.wkclz.micro.dag.bean.entity.DagNodeInstance;
import com.wkclz.micro.dag.bean.enums.ExecutionEvent;
import com.wkclz.micro.dag.config.DagProperties;
import com.wkclz.micro.dag.core.DagReadyCalculator;
import com.wkclz.micro.dag.mapper.DagInstanceMapper;
import com.wkclz.micro.dag.mapper.DagNodeInstanceMapper;
import com.wkclz.micro.dag.service.DagDefinitionService;
import com.wkclz.micro.dag.service.DagExecutionLogService;
import com.wkclz.micro.dag.worker.DagWorkerExecutor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class DagSchedulerTest {

    private static final String INSTANCE_CODE = "INST20261114000001";

    private DagInstanceMapper instanceMapper;
    private DagNodeInstanceMapper nodeMapper;
    private DagDefinitionService definitionService;
    private DagExecutionLogService logService;
    private DagReadyCalculator readyCalculator;
    private DagWorkerExecutor worker;
    private DagProperties properties;
    private DagScheduler scheduler;

    @BeforeEach
    void setUp() {
        instanceMapper = mock(DagInstanceMapper.class);
        nodeMapper = mock(DagNodeInstanceMapper.class);
        definitionService = mock(DagDefinitionService.class);
        logService = mock(DagExecutionLogService.class);
        readyCalculator = mock(DagReadyCalculator.class);
        worker = mock(DagWorkerExecutor.class);
        properties = new DagProperties();
        scheduler = new DagScheduler(instanceMapper, nodeMapper, definitionService, logService,
            readyCalculator, worker, properties);
    }

    @Test
    void tick_pubsubChannel_skipsScan() {
        properties.setChannel("pubsub");

        scheduler.tick();

        verify(nodeMapper, never()).promoteRetrying(any());
    }

    @Test
    void tick_defaultChannel_scans() {
        when(instanceMapper.listRunning()).thenReturn(List.of());

        scheduler.tick();

        verify(nodeMapper).promoteRetrying(any());
    }

    @Test
    void wakeUp_dbChannel_ignored() {
        properties.setChannel("db");

        scheduler.wakeUp();

        verify(nodeMapper, never()).promoteRetrying(any());
    }

    @Test
    void wakeUp_autoChannel_scans() {
        when(instanceMapper.listRunning()).thenReturn(List.of());

        scheduler.wakeUp();

        verify(nodeMapper).promoteRetrying(any());
    }

    @Test
    void scan_claimsReadyNodeAndSubmits() {
        DagInstance instance = instance();
        DagNodeInstance node = node("A", null);
        when(instanceMapper.listRunning()).thenReturn(List.of(instance));
        when(nodeMapper.listByInstanceCode(INSTANCE_CODE)).thenReturn(List.of(node));
        when(readyCalculator.readyNodes(any(), any(), any(), any())).thenReturn(Set.of("A"));
        when(nodeMapper.casClaim(any(), any(), anyString(), any())).thenReturn(1);

        scheduler.scan();

        ArgumentCaptor<DagNodeInstance> captor = ArgumentCaptor.forClass(DagNodeInstance.class);
        verify(worker).submit(captor.capture(), eq(instance));
        DagNodeInstance submitted = captor.getValue();
        assertEquals("RUNNING", submitted.getStatus());
        assertEquals(1, submitted.getAttempt());
        assertNotNull(submitted.getLeaseOwner());
        assertNotNull(submitted.getLeaseExpireTime());
        verify(logService).log(eq(INSTANCE_CODE), eq("A"), eq(1), eq(ExecutionEvent.CLAIM), isNull(), anyString());
    }

    @Test
    void scan_claimFailed_skipsSubmit() {
        DagInstance instance = instance();
        DagNodeInstance node = node("A", null);
        when(instanceMapper.listRunning()).thenReturn(List.of(instance));
        when(nodeMapper.listByInstanceCode(INSTANCE_CODE)).thenReturn(List.of(node));
        when(readyCalculator.readyNodes(any(), any(), any(), any())).thenReturn(Set.of("A"));
        when(nodeMapper.casClaim(any(), any(), anyString(), any())).thenReturn(0);

        scheduler.scan();

        verify(worker, never()).submit(any(), any());
        verify(logService, never()).log(any(), any(), any(), any(), any(), any());
    }

    @Test
    void scan_notReadyNode_skipped() {
        DagInstance instance = instance();
        DagNodeInstance node = node("A", null);
        when(instanceMapper.listRunning()).thenReturn(List.of(instance));
        when(nodeMapper.listByInstanceCode(INSTANCE_CODE)).thenReturn(List.of(node));
        when(readyCalculator.readyNodes(any(), any(), any(), any())).thenReturn(Set.of());

        scheduler.scan();

        verify(nodeMapper, never()).casClaim(any(), any(), anyString(), any());
        verify(worker, never()).submit(any(), any());
    }

    @Test
    void scan_batchSizeLimitsClaims() {
        properties.getScheduler().setBatchSize(1);
        DagInstance instance = instance();
        DagNodeInstance a = node("A", null);
        DagNodeInstance b = node("B", null);
        when(instanceMapper.listRunning()).thenReturn(List.of(instance));
        when(nodeMapper.listByInstanceCode(INSTANCE_CODE)).thenReturn(List.of(a, b));
        when(readyCalculator.readyNodes(any(), any(), any(), any())).thenReturn(Set.of("A", "B"));
        when(nodeMapper.casClaim(any(), any(), anyString(), any())).thenReturn(1);

        scheduler.scan();

        verify(nodeMapper, times(1)).casClaim(any(), any(), anyString(), any());
        verify(worker, times(1)).submit(any(), any());
    }

    @Test
    void scan_reclaimsExpiredLease() {
        DagInstance instance = instance();
        DagNodeInstance expired = node("A", null);
        expired.setStatus("RUNNING");
        expired.setLeaseOwner("other-worker");
        when(instanceMapper.listRunning()).thenReturn(List.of());
        when(nodeMapper.listExpiredLease(any())).thenReturn(List.of(expired));
        when(nodeMapper.reclaimExpiredLease(any())).thenReturn(1);

        scheduler.scan();

        verify(nodeMapper).reclaimExpiredLease(any());
        verify(logService).log(eq(INSTANCE_CODE), eq("A"), any(), eq(ExecutionEvent.LEASE_RECLAIM),
            isNull(), eq("other-worker"));
        // 回收后 PENDING，本轮不再领取（无运行实例）
        verify(worker, never()).submit(any(), any());
    }

    @Test
    void scan_instanceError_continuesOthers() {
        DagInstance bad = instance();
        bad.setDagCode("d1");
        DagInstance good = instance();
        good.setDagCode("d2");
        good.setInstanceCode("INST20261114000002");
        DagNodeInstance node = node("A", null);
        when(instanceMapper.listRunning()).thenReturn(List.of(bad, good));
        when(nodeMapper.listByInstanceCode(anyString())).thenReturn(List.of(node));
        when(definitionService.load(eq("d1"), any())).thenThrow(new IllegalStateException("boom"));
        when(definitionService.load(eq("d2"), any())).thenReturn(new DagDefModel());
        when(readyCalculator.readyNodes(any(), any(), any(), any())).thenReturn(Set.of("A"));
        when(nodeMapper.casClaim(any(), any(), anyString(), any())).thenReturn(1);

        scheduler.scan();

        verify(worker, times(1)).submit(any(), any());
    }

    private DagInstance instance() {
        DagInstance instance = new DagInstance();
        instance.setInstanceCode(INSTANCE_CODE);
        instance.setDagCode("d1");
        instance.setDagVersion(1);
        instance.setStatus("RUNNING");
        return instance;
    }

    private DagNodeInstance node(String nodeCode, Long timeoutMs) {
        DagNodeInstance node = new DagNodeInstance();
        node.setId(1L);
        node.setInstanceCode(INSTANCE_CODE);
        node.setNodeCode(nodeCode);
        node.setHandler("h_" + nodeCode);
        node.setStatus("PENDING");
        node.setAttempt(null);
        node.setTimeoutMs(timeoutMs);
        node.setVersion(0);
        return node;
    }
}
