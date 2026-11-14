package com.wkclz.micro.dag.service;

import com.wkclz.core.exception.ValidationException;
import com.wkclz.micro.dag.api.NodeHandlerRegistry;
import com.wkclz.micro.dag.bean.entity.DagDefinition;
import com.wkclz.micro.dag.bean.entity.DagNode;
import com.wkclz.micro.dag.core.DagValidator;
import com.wkclz.micro.dag.mapper.DagDefinitionMapper;
import com.wkclz.micro.dag.mapper.DagEdgeMapper;
import com.wkclz.micro.dag.mapper.DagNodeMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class DagDefinitionServiceTest {

    private DagDefinitionService service;
    private DagDefinitionMapper mapper;
    private DagNodeMapper nodeMapper;
    private DagEdgeMapper edgeMapper;
    private NodeHandlerRegistry registry;

    @BeforeEach
    void setUp() {
        mapper = mock(DagDefinitionMapper.class);
        nodeMapper = mock(DagNodeMapper.class);
        edgeMapper = mock(DagEdgeMapper.class);
        registry = mock(NodeHandlerRegistry.class);
        when(registry.codes()).thenReturn(Set.of("h1"));
        service = new DagDefinitionService(new DagValidator(), registry, nodeMapper, edgeMapper);
        ReflectionTestUtils.setField(service, "mapper", mapper);
    }

    private DagDefinition draft() {
        DagDefinition d = new DagDefinition();
        d.setId(1L);
        d.setDagCode("d1");
        d.setDagName("demo");
        d.setDagVersion(0);
        d.setStatus("DRAFT");
        return d;
    }

    private DagNode nodeRow(String code) {
        DagNode row = new DagNode();
        row.setDagCode("d1");
        row.setDagVersion(0);
        row.setNodeCode(code);
        row.setNodeName(code);
        row.setHandler("h1");
        return row;
    }

    @Test
    void publish_notDraft_throws() {
        DagDefinition d = draft();
        d.setStatus("PUBLISHED");
        when(mapper.selectById(1L)).thenReturn(d);
        assertThrows(ValidationException.class, () -> service.publish(1L));
    }

    @Test
    void publish_success_versionIncrement() {
        DagDefinition d = draft();
        when(mapper.selectById(1L)).thenReturn(d);
        when(mapper.selectOneByEntity(any())).thenReturn(d);
        when(nodeMapper.selectByEntity(any())).thenReturn(List.of(nodeRow("a"), nodeRow("b")));
        when(edgeMapper.selectByEntity(any())).thenReturn(List.of());
        DagDefinition existing = new DagDefinition();
        existing.setDagVersion(1);
        when(mapper.selectByEntity(any())).thenReturn(List.of(existing));

        Integer version = service.publish(1L);

        assertEquals(2, version);
        assertEquals("PUBLISHED", d.getStatus());
    }
}
