package com.wkclz.micro.dag.service;

import com.wkclz.core.exception.ValidationException;
import com.wkclz.micro.dag.bean.entity.DagExecutionLog;
import com.wkclz.micro.dag.bean.entity.DagNodeInstance;
import com.wkclz.micro.dag.bean.resp.NodeDetailResp;
import com.wkclz.micro.dag.mapper.DagNodeInstanceMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class DagNodeInstanceServiceTest {

    private static final String INSTANCE_CODE = "INST20261114000001";
    private static final String NODE_CODE = "N_pay";

    private DagNodeInstanceService service;
    private DagNodeInstanceMapper mapper;
    private DagExecutionLogService logService;

    @BeforeEach
    void setUp() {
        mapper = mock(DagNodeInstanceMapper.class);
        logService = mock(DagExecutionLogService.class);
        service = new DagNodeInstanceService(logService);
        ReflectionTestUtils.setField(service, "mapper", mapper);
    }

    @Test
    void nodeDetail_aggregatesLogs() {
        DagNodeInstance node = new DagNodeInstance();
        node.setId(1L);
        node.setInstanceCode(INSTANCE_CODE);
        node.setNodeCode(NODE_CODE);
        node.setStatus("RUNNING");
        node.setAttempt(1);
        when(mapper.selectOneByEntity(any())).thenReturn(node);

        DagExecutionLog log = new DagExecutionLog();
        log.setInstanceCode(INSTANCE_CODE);
        log.setNodeCode(NODE_CODE);
        log.setAttempt(1);
        log.setEvent("CLAIM");
        log.setWorker("w1");
        when(logService.listByNode(INSTANCE_CODE, NODE_CODE)).thenReturn(List.of(log));

        NodeDetailResp resp = service.nodeDetail(INSTANCE_CODE, NODE_CODE);

        assertEquals(NODE_CODE, resp.getNode().getNodeCode());
        assertEquals("RUNNING", resp.getNode().getStatus());
        assertEquals(1, resp.getLogs().size());
        assertEquals("CLAIM", resp.getLogs().get(0).getEvent());
    }

    @Test
    void nodeDetail_notExist_throws() {
        when(mapper.selectOneByEntity(any())).thenReturn(null);
        assertThrows(ValidationException.class, () -> service.nodeDetail(INSTANCE_CODE, NODE_CODE));
    }
}
