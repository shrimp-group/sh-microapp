package com.wkclz.micro.dag.service;

import com.wkclz.core.exception.ValidationException;
import com.wkclz.micro.dag.bean.dto.DagDefModel;
import com.wkclz.micro.dag.bean.dto.EdgeDef;
import com.wkclz.micro.dag.bean.dto.NodeDef;
import com.wkclz.micro.dag.bean.entity.DagDefinition;
import com.wkclz.micro.dag.bean.entity.DagInstance;
import com.wkclz.micro.dag.bean.entity.DagNodeInstance;
import com.wkclz.micro.dag.bean.req.LaunchReq;
import com.wkclz.micro.dag.mapper.DagInstanceMapper;
import com.wkclz.micro.dag.mapper.DagNodeInstanceMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class DagInstanceServiceTest {

    private DagInstanceService service;
    private DagInstanceMapper mapper;
    private DagNodeInstanceMapper nodeInstanceMapper;
    private DagDefinitionService definitionService;

    @BeforeEach
    void setUp() {
        mapper = mock(DagInstanceMapper.class);
        nodeInstanceMapper = mock(DagNodeInstanceMapper.class);
        definitionService = mock(DagDefinitionService.class);
        service = new DagInstanceService(definitionService, nodeInstanceMapper);
        ReflectionTestUtils.setField(service, "mapper", mapper);

        DagDefinition def = new DagDefinition();
        def.setDagCode("d1");
        def.setDagVersion(3);
        def.setStatus("PUBLISHED");
        when(definitionService.selectByEntity(any())).thenReturn(List.of(def));
        when(definitionService.load("d1", 3)).thenReturn(defModel());
    }

    /** 组装 d1 v3 的内存定义：a --普通边--> b，a 带重试快照 */
    private DagDefModel defModel() {
        NodeDef a = new NodeDef();
        a.setCode("a");
        a.setName("a");
        a.setHandler("h1");
        a.setMaxRetry(2);
        a.setIntervalSec(30);
        a.setBackoff("FIXED");
        NodeDef b = new NodeDef();
        b.setCode("b");
        b.setName("b");
        b.setHandler("h1");
        EdgeDef ab = new EdgeDef();
        ab.setEdgeCode("a->b");
        ab.setFromCode("a");
        ab.setToCode("b");
        ab.setType(EdgeDef.TYPE_NORMAL);
        DagDefModel def = new DagDefModel();
        def.setDagCode("d1");
        def.setDagName("demo");
        def.setDagVersion(3);
        def.setNodes(List.of(a, b));
        def.setEdges(List.of(ab));
        return def;
    }

    private LaunchReq launchReq() {
        LaunchReq req = new LaunchReq();
        req.setDagCode("d1");
        req.setTraceCode("TRC-001");
        req.setParams(Map.of("sourceType", "api"));
        return req;
    }

    @Test
    void launch_createsInstanceAndNodes() {
        when(mapper.selectCountByEntity(any())).thenReturn(0L);

        service.launch(launchReq());

        ArgumentCaptor<DagInstance> insCap = ArgumentCaptor.forClass(DagInstance.class);
        verify(mapper).insert(insCap.capture());
        assertEquals("RUNNING", insCap.getValue().getStatus());
        assertEquals(3, insCap.getValue().getDagVersion());
        assertTrue(insCap.getValue().getInstanceCode().startsWith("INST"));

        ArgumentCaptor<List<DagNodeInstance>> nodeCap = ArgumentCaptor.captor();
        verify(nodeInstanceMapper).insertBatch(nodeCap.capture());
        List<DagNodeInstance> nodes = nodeCap.getValue();
        assertEquals(2, nodes.size());
        DagNodeInstance a = nodes.get(0);
        assertEquals("PENDING", a.getStatus());
        assertEquals(2, a.getMaxRetry());
        assertEquals(0, a.getCleanupFlag());
        // 节点实例的实例编码与实例一致
        assertEquals(a.getInstanceCode(), nodes.get(1).getInstanceCode());
    }

    @Test
    void launch_duplicateTraceCode_throws() {
        when(mapper.selectCountByEntity(any())).thenReturn(1L);
        assertThrows(ValidationException.class, () -> service.launch(launchReq()));
    }

    @Test
    void launch_noPublishedDefinition_throws() {
        when(mapper.selectCountByEntity(any())).thenReturn(0L);
        when(definitionService.selectByEntity(any())).thenReturn(List.of());
        assertThrows(ValidationException.class, () -> service.launch(launchReq()));
    }
}
