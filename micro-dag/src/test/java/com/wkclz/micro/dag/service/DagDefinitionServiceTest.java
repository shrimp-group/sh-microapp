package com.wkclz.micro.dag.service;

import com.wkclz.core.exception.ValidationException;
import com.wkclz.micro.dag.api.NodeContext;
import com.wkclz.micro.dag.api.NodeHandler;
import com.wkclz.micro.dag.api.NodeHandlerRegistry;
import com.wkclz.micro.dag.api.NodeResult;
import com.wkclz.micro.dag.bean.dto.EdgeDef;
import com.wkclz.micro.dag.bean.dto.NodeDef;
import com.wkclz.micro.dag.bean.entity.DagDefinition;
import com.wkclz.micro.dag.bean.entity.DagNode;
import com.wkclz.micro.dag.bean.enums.NodeType;
import com.wkclz.micro.dag.bean.req.DefinitionCreateReq;
import com.wkclz.micro.dag.bean.req.DefinitionUpdateReq;
import com.wkclz.micro.dag.bean.resp.DefinitionCreateResp;
import com.wkclz.micro.dag.core.DagValidator;
import com.wkclz.micro.dag.mapper.DagDefinitionMapper;
import com.wkclz.micro.dag.mapper.DagEdgeMapper;
import com.wkclz.micro.dag.mapper.DagNodeMapper;
import com.wkclz.redis.helper.RedisIdGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class DagDefinitionServiceTest {

    /** 与 RedisIdGenerator.BASE32_CHARS 一致的 Crockford 风格小写表 */
    private static final String BASE32_CHARS = "0123456789abcdefghjkmnpqrstvwxyz";

    private DagDefinitionService service;
    private DagDefinitionMapper mapper;
    private DagNodeMapper nodeMapper;
    private DagEdgeMapper edgeMapper;
    private NodeHandlerRegistry registry;
    private RedisIdGenerator idGenerator;

    @BeforeEach
    void setUp() {
        mapper = mock(DagDefinitionMapper.class);
        nodeMapper = mock(DagNodeMapper.class);
        edgeMapper = mock(DagEdgeMapper.class);
        registry = mock(NodeHandlerRegistry.class);
        // 含系统 handler（system-start/system-end 永久注册，特殊节点带 handler 时需已注册）
        when(registry.codes()).thenReturn(Set.of("h1", "system-start", "system-end"));
        // 规则 8 按 handler.paramSchema 校验 params：占位 handler 无 schema，保证既有用例不被参数校验拦截
        when(registry.get(any(String.class))).thenAnswer(inv -> new NodeHandler() {
            @Override
            public String nodeCode() { return String.valueOf(inv.getArgument(0)); }
            @Override
            public NodeResult execute(NodeContext ctx) { return NodeResult.ok(Map.of()); }
        });
        idGenerator = mock(RedisIdGenerator.class);
        when(idGenerator.generateBase32IdWithPrefix("dag_")).thenReturn("dag_test123");
        service = new DagDefinitionService(new DagValidator(), registry, nodeMapper, edgeMapper, idGenerator);
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

    private NodeDef node(String code, String nodeType, String handler) {
        NodeDef n = new NodeDef();
        n.setCode(code);
        n.setName(code);
        n.setNodeType(nodeType);
        n.setHandler(handler);
        return n;
    }

    private EdgeDef edge(String from, String to) {
        EdgeDef e = new EdgeDef();
        e.setFromCode(from);
        e.setToCode(to);
        return e;
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
        // 节点/边行随发布版本重挂，新版本可 load（否则发布后设计详情报"定义无节点"）
        ArgumentCaptor<DagNode> nodeCaptor = ArgumentCaptor.forClass(DagNode.class);
        verify(nodeMapper, times(2)).insert(nodeCaptor.capture());
        for (DagNode row : nodeCaptor.getAllValues()) {
            assertEquals(2, row.getDagVersion());
        }
    }

    @Test
    void create_success_returnsIdAndDagCode() {
        when(mapper.insert(any(DagDefinition.class))).thenAnswer(inv -> {
            DagDefinition e = inv.getArgument(0);
            e.setId(100L);
            return 1;
        });
        when(nodeMapper.selectByEntity(any())).thenReturn(List.of());
        when(edgeMapper.selectByEntity(any())).thenReturn(List.of());

        DefinitionCreateReq req = new DefinitionCreateReq();
        req.setDagName("demo");
        // 复现用户原报错场景：节点携带 nodeType 提交（START↔system-start、END↔system-end）
        req.setNodes(List.of(
            node("system-start", NodeType.START.getCode(), "system-start"),
            node("system-end", NodeType.END.getCode(), "system-end")));
        req.setEdges(List.of(edge("system-start", "system-end")));

        DefinitionCreateResp resp = service.create(req);

        assertEquals(100L, resp.getId());
        assertEquals("dag_test123", resp.getDagCode());
        assertTrue(resp.getDagCode().startsWith("dag_") && resp.getDagCode().length() > "dag_".length());

        ArgumentCaptor<DagDefinition> defCaptor = ArgumentCaptor.forClass(DagDefinition.class);
        verify(mapper).insert(defCaptor.capture());
        assertEquals("dag_test123", defCaptor.getValue().getDagCode());
        assertEquals(0, defCaptor.getValue().getDagVersion());
        assertEquals("DRAFT", defCaptor.getValue().getStatus());

        // 落库节点 nodeType 不为 null
        ArgumentCaptor<DagNode> nodeCaptor = ArgumentCaptor.forClass(DagNode.class);
        verify(nodeMapper, times(2)).insert(nodeCaptor.capture());
        for (DagNode row : nodeCaptor.getAllValues()) {
            assertNotNull(row.getNodeType());
        }
    }

    @Test
    void create_blankDagName_throws() {
        DefinitionCreateReq req = new DefinitionCreateReq();
        req.setDagName(" ");
        assertThrows(ValidationException.class, () -> service.create(req));
    }

    @Test
    void create_dagCode_realGeneratorBase32() {
        // 真实实例：Redis 不可用时走本地降级，不影响编码结果
        RedisIdGenerator generator = new RedisIdGenerator();
        String dagCode = generator.generateBase32IdWithPrefix("dag_");
        assertTrue(dagCode.startsWith("dag_"));
        String encoded = dagCode.substring("dag_".length());
        assertFalse(encoded.isEmpty());
        for (char c : encoded.toCharArray()) {
            assertTrue(BASE32_CHARS.indexOf(c) >= 0, "非法 Base32 字符: " + c);
        }
    }

    @Test
    void update_success_dagCodeUnchanged() {
        DagDefinition d = draft();
        when(mapper.selectById(1L)).thenReturn(d);
        when(nodeMapper.selectByEntity(any())).thenReturn(List.of());
        when(edgeMapper.selectByEntity(any())).thenReturn(List.of());

        DefinitionUpdateReq req = new DefinitionUpdateReq();
        req.setId(1L);
        req.setDagName("demo2");
        req.setNodes(List.of(node("a", null, "h1"), node("b", null, "h1")));
        req.setEdges(List.of(edge("a", "b")));

        Long id = service.update(req);

        assertEquals(1L, id);
        // dagCode 沿用库中已有值
        assertEquals("d1", d.getDagCode());
        assertEquals("demo2", d.getDagName());
    }

    @Test
    void update_idNull_throws() {
        DefinitionUpdateReq req = new DefinitionUpdateReq();
        assertThrows(ValidationException.class, () -> service.update(req));
    }

    @Test
    void update_notDraft_throws() {
        DagDefinition d = draft();
        d.setStatus("PUBLISHED");
        when(mapper.selectById(1L)).thenReturn(d);

        DefinitionUpdateReq req = new DefinitionUpdateReq();
        req.setId(1L);
        req.setDagName("demo2");
        req.setNodes(List.of(node("a", null, "h1")));

        assertThrows(ValidationException.class, () -> service.update(req));
    }
}
