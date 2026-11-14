package com.wkclz.micro.dag.service;

import com.wkclz.core.exception.ValidationException;
import com.wkclz.micro.dag.bean.dto.DagDefModel;
import com.wkclz.micro.dag.bean.dto.EdgeDef;
import com.wkclz.micro.dag.bean.entity.DagInstance;
import com.wkclz.micro.dag.bean.entity.DagNodeInstance;
import com.wkclz.micro.dag.bean.enums.ExecutionEvent;
import com.wkclz.micro.dag.bean.req.NodeInterveneReq;
import com.wkclz.micro.dag.cache.DagRedisPublisher;
import com.wkclz.micro.dag.mapper.DagNodeInstanceMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class DagInterventionServiceTest {

    private static final String INSTANCE_CODE = "INST20261114000001";
    private static final String NODE_CODE = "N_pay";

    private DagNodeInstanceMapper mapper;
    private DagInstanceService instanceService;
    private DagDefinitionService definitionService;
    private DagEngineService engine;
    private DagExecutionLogService logService;
    private DagRedisPublisher publisher;
    private DagInterventionService service;

    @BeforeEach
    void setUp() {
        mapper = mock(DagNodeInstanceMapper.class);
        instanceService = mock(DagInstanceService.class);
        definitionService = mock(DagDefinitionService.class);
        engine = mock(DagEngineService.class);
        logService = mock(DagExecutionLogService.class);
        publisher = mock(DagRedisPublisher.class);
        service = new DagInterventionService(instanceService, definitionService, engine, logService, publisher);
        ReflectionTestUtils.setField(service, "mapper", mapper);
    }

    @Test
    void interrupt_running_setsFlagAndBroadcasts() {
        DagNodeInstance node = node(NODE_CODE, "RUNNING", 1);
        when(mapper.selectOneByEntity(any())).thenReturn(node);

        service.interrupt(INSTANCE_CODE, NODE_CODE);

        assertEquals(1, node.getInterruptFlag());
        verify(mapper).updateById(node);
        verify(publisher).publishInterrupt(INSTANCE_CODE, NODE_CODE);
    }

    @Test
    void interrupt_notRunning_throws() {
        when(mapper.selectOneByEntity(any())).thenReturn(node(NODE_CODE, "SUCCESS", 1));

        assertThrows(ValidationException.class, () -> service.interrupt(INSTANCE_CODE, NODE_CODE));
        verify(publisher, never()).publishInterrupt(any(), any());
    }

    @Test
    void params_running_throws() {
        when(mapper.selectOneByEntity(any())).thenReturn(node(NODE_CODE, "RUNNING", 1));

        assertThrows(ValidationException.class, () -> service.params(req(null)));
    }

    @Test
    void params_updatesInput() {
        DagNodeInstance node = node(NODE_CODE, "FAILED", 1);
        when(mapper.selectOneByEntity(any())).thenReturn(node);

        service.params(req(null));

        assertEquals("{\"k\":\"v\"}", node.getInputParamsJson());
        verify(mapper).updateById(node);
    }

    @Test
    void skip_setsSkippedAndAdvances() {
        DagNodeInstance node = node(NODE_CODE, "FAILED", 1);
        when(mapper.selectOneByEntity(any())).thenReturn(node);

        service.skip(req(null));

        assertEquals("SKIPPED", node.getStatus());
        assertEquals("人工跳过", node.getSkipReason());
        verify(logService).log(eq(INSTANCE_CODE), eq(NODE_CODE), eq(1), eq(ExecutionEvent.SKIP), eq(null), eq(null));
        verify(engine).advance(INSTANCE_CODE);
    }

    @Test
    void skip_running_throws() {
        when(mapper.selectOneByEntity(any())).thenReturn(node(NODE_CODE, "RUNNING", 1));

        assertThrows(ValidationException.class, () -> service.skip(req(null)));
    }

    @Test
    void reset_defaultKeepsParams_clearsResults() {
        DagNodeInstance node = node(NODE_CODE, "FAILED", 2);
        node.setOutputJson("{}");
        node.setErrorMsg("boom");
        node.setInputParamsJson("{\"k\":\"v\"}");
        when(mapper.selectOneByEntity(any())).thenReturn(node);

        service.reset(req(false));

        assertEquals("PENDING", node.getStatus());
        assertEquals(null, node.getOutputJson());
        assertEquals(null, node.getErrorMsg());
        assertEquals("{\"k\":\"v\"}", node.getInputParamsJson());
        verify(logService).log(eq(INSTANCE_CODE), eq(NODE_CODE), eq(2), eq(ExecutionEvent.RESET), eq(null), eq(null));
    }

    @Test
    void reset_clearParamsClearsInput() {
        DagNodeInstance node = node(NODE_CODE, "SUCCESS", 2);
        node.setInputParamsJson("{\"k\":\"v\"}");
        when(mapper.selectOneByEntity(any())).thenReturn(node);

        service.reset(req(true));

        assertEquals("PENDING", node.getStatus());
        assertEquals(null, node.getInputParamsJson());
    }

    @Test
    void rerun_resetsAndResumesInstance() {
        DagNodeInstance node = node(NODE_CODE, "FAILED", 2);
        when(mapper.selectOneByEntity(any())).thenReturn(node);
        DagInstance instance = instance("FAILED");
        when(instanceService.selectOneByEntity(any())).thenReturn(instance);

        service.rerun(INSTANCE_CODE, NODE_CODE);

        assertEquals("PENDING", node.getStatus());
        assertEquals(1, node.getCleanupFlag());
        assertEquals(0, node.getInterruptFlag());
        assertEquals("RUNNING", instance.getStatus());
        verify(instanceService).updateById(any());
        verify(publisher).wakeUp();
    }

    @Test
    void rerun_noHistory_noCleanupFlag() {
        DagNodeInstance node = node(NODE_CODE, "SKIPPED", null);
        when(mapper.selectOneByEntity(any())).thenReturn(node);
        when(instanceService.selectOneByEntity(any())).thenReturn(instance("FAILED"));

        service.rerun(INSTANCE_CODE, NODE_CODE);

        assertEquals(0, node.getCleanupFlag());
    }

    @Test
    void rerun_running_throws() {
        when(mapper.selectOneByEntity(any())).thenReturn(node(NODE_CODE, "RUNNING", 1));

        assertThrows(ValidationException.class, () -> service.rerun(INSTANCE_CODE, NODE_CODE));
    }

    @Test
    void rerunFrom_resetsDownstreamClosure_only() {
        // A → B → C 链，D 独立；从 A 重跑应重置 A/B/C，保留 D 与参数
        when(instanceService.selectOneByEntity(any())).thenReturn(instance("FAILED"));
        DagDefModel def = new DagDefModel();
        def.setEdges(List.of(edge("A", "B"), edge("B", "C")));
        when(definitionService.load("d1", 1)).thenReturn(def);
        DagNodeInstance a = node("A", "SUCCESS", 1);
        DagNodeInstance b = node("B", "SUCCESS", 1);
        DagNodeInstance c = node("C", "FAILED", 1);
        DagNodeInstance d = node("D", "SUCCESS", 1);
        when(mapper.listByInstanceCode(INSTANCE_CODE)).thenReturn(List.of(a, b, c, d));

        service.rerunFrom(INSTANCE_CODE, "A");

        ArgumentCaptor<DagNodeInstance> captor = ArgumentCaptor.forClass(DagNodeInstance.class);
        verify(mapper, times(3)).updateById(captor.capture());
        for (DagNodeInstance n : captor.getAllValues()) {
            assertEquals("PENDING", n.getStatus());
            assertEquals(1, n.getCleanupFlag());
        }
        assertEquals("SUCCESS", d.getStatus());
        verify(instanceService).updateById(any());
        verify(publisher).wakeUp();
    }

    @Test
    void rerunFrom_runningInstance_throws() {
        when(instanceService.selectOneByEntity(any())).thenReturn(instance("RUNNING"));

        assertThrows(ValidationException.class, () -> service.rerunFrom(INSTANCE_CODE, "A"));
    }

    @Test
    void terminate_running_stopsRunningNodes() {
        when(instanceService.selectOneByEntity(any())).thenReturn(instance("RUNNING"));
        DagNodeInstance running = node("A", "RUNNING", 1);
        DagNodeInstance success = node("B", "SUCCESS", 1);
        when(mapper.listByInstanceCode(INSTANCE_CODE)).thenReturn(List.of(running, success));

        service.terminate(INSTANCE_CODE);

        verify(instanceService).updateById(any());
        assertEquals(1, running.getInterruptFlag());
        verify(publisher).publishInterrupt(INSTANCE_CODE, "A");
        verify(publisher, never()).publishInterrupt(INSTANCE_CODE, "B");
    }

    @Test
    void terminate_failed_noBroadcast() {
        when(instanceService.selectOneByEntity(any())).thenReturn(instance("FAILED"));

        service.terminate(INSTANCE_CODE);

        verify(instanceService).updateById(any());
        verify(publisher, never()).publishInterrupt(any(), any());
    }

    @Test
    void terminate_successInstance_throws() {
        when(instanceService.selectOneByEntity(any())).thenReturn(instance("SUCCESS"));

        assertThrows(ValidationException.class, () -> service.terminate(INSTANCE_CODE));
    }

    private DagNodeInstance node(String nodeCode, String status, Integer attempt) {
        DagNodeInstance node = new DagNodeInstance();
        node.setId(1L);
        node.setInstanceCode(INSTANCE_CODE);
        node.setNodeCode(nodeCode);
        node.setHandler("h_" + nodeCode);
        node.setStatus(status);
        node.setAttempt(attempt);
        return node;
    }

    private DagInstance instance(String status) {
        DagInstance instance = new DagInstance();
        instance.setInstanceCode(INSTANCE_CODE);
        instance.setDagCode("d1");
        instance.setDagVersion(1);
        instance.setStatus(status);
        return instance;
    }

    private NodeInterveneReq req(Boolean clearParams) {
        NodeInterveneReq req = new NodeInterveneReq();
        req.setInstanceCode(INSTANCE_CODE);
        req.setNodeCode(NODE_CODE);
        req.setInputParamsJson("{\"k\":\"v\"}");
        req.setClearParams(clearParams);
        return req;
    }

    private EdgeDef edge(String from, String to) {
        EdgeDef edge = new EdgeDef();
        edge.setFromCode(from);
        edge.setEdgeCode(from + "->" + to);
        edge.setToCode(to);
        return edge;
    }
}
