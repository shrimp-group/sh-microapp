package com.wkclz.micro.dag.service;

import com.wkclz.core.exception.ValidationException;
import com.wkclz.micro.dag.bean.entity.DagNodeInstance;
import com.wkclz.micro.dag.bean.enums.ExecutionEvent;
import com.wkclz.micro.dag.cache.DagRedisPublisher;
import com.wkclz.micro.dag.core.DagExpressionEvaluator;
import com.wkclz.micro.dag.core.DagReadyCalculator;
import com.wkclz.micro.dag.core.DagRetryCalculator;
import com.wkclz.micro.dag.mapper.DagNodeInstanceMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class DagEngineServiceTest {

    private static final String INSTANCE_CODE = "INST20261114000001";
    private static final String NODE_CODE = "N_pay";

    private DagEngineService engine;
    private DagNodeInstanceMapper nodeMapper;
    private DagInstanceService instanceService;
    private DagDefinitionService definitionService;

    @BeforeEach
    void setUp() {
        nodeMapper = mock(DagNodeInstanceMapper.class);
        instanceService = mock(DagInstanceService.class);
        definitionService = mock(DagDefinitionService.class);
        DagExecutionLogService logService = mock(DagExecutionLogService.class);
        DagRedisPublisher publisher = mock(DagRedisPublisher.class);
        engine = new DagEngineService(instanceService, definitionService, logService,
            new DagReadyCalculator(new DagExpressionEvaluator()), new DagRetryCalculator(), publisher);
        ReflectionTestUtils.setField(engine, "mapper", nodeMapper);
    }

    @Test
    void onSuccess_duplicateCallback_ignored() {
        // 节点已非 RUNNING（防重投递）
        DagNodeInstance node = new DagNodeInstance();
        node.setId(1L);
        node.setInstanceCode(INSTANCE_CODE);
        node.setNodeCode(NODE_CODE);
        node.setStatus("SUCCESS");
        when(nodeMapper.selectOneByEntity(any())).thenReturn(node);

        assertDoesNotThrow(() -> engine.onSuccess(INSTANCE_CODE, NODE_CODE, Map.of()));
        Mockito.verify(nodeMapper, Mockito.never()).updateById(any());
    }

    @Test
    void onFailure_retryExhausted_failed() {
        DagNodeInstance node = new DagNodeInstance();
        node.setId(1L);
        node.setInstanceCode(INSTANCE_CODE);
        node.setNodeCode(NODE_CODE);
        node.setStatus("RUNNING");
        node.setAttempt(3);
        node.setMaxRetry(2);
        node.setBackoff("FIXED");
        node.setIntervalSec(30);
        when(nodeMapper.selectOneByEntity(any())).thenReturn(node);

        engine.onFailure(INSTANCE_CODE, NODE_CODE, ExecutionEvent.FAIL, "boom");

        Mockito.verify(nodeMapper).updateById(any());
        Mockito.verify(instanceService, Mockito.never()).updateById(any());
    }

    @Test
    void onSuccess_unknownNode_throws() {
        when(nodeMapper.selectOneByEntity(any())).thenReturn(null);
        assertThrows(ValidationException.class, () -> engine.onSuccess(INSTANCE_CODE, "N_ghost", null));
    }

    @Test
    void instanceTerminalStatus_helper() {
        // 直接复用 DagReadyCalculator 已测逻辑，此处仅防回归
        Optional<String> s = new DagReadyCalculator(new DagExpressionEvaluator())
            .instanceTerminalStatus(Map.of("a", com.wkclz.micro.dag.bean.enums.NodeStatus.FAILED));
        assertEquals("FAILED", s.get());
    }
}
