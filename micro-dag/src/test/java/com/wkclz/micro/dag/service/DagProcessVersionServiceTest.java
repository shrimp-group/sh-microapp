package com.wkclz.micro.dag.service;

import com.wkclz.core.exception.ValidationException;
import com.wkclz.micro.dag.api.NodeContext;
import com.wkclz.micro.dag.api.NodeHandler;
import com.wkclz.micro.dag.api.NodeHandlerRegistry;
import com.wkclz.micro.dag.api.NodeResult;
import com.wkclz.micro.dag.bean.dto.EdgeDef;
import com.wkclz.micro.dag.bean.dto.NodeDef;
import com.wkclz.micro.dag.bean.entity.DagProcess;
import com.wkclz.micro.dag.bean.entity.DagProcessVersion;
import com.wkclz.micro.dag.bean.entity.DagEdge;
import com.wkclz.micro.dag.bean.entity.DagNode;
import com.wkclz.micro.dag.bean.enums.NodeType;
import com.wkclz.micro.dag.bean.enums.ProcessStatus;
import com.wkclz.micro.dag.bean.req.ProcessVersionCreateReq;
import com.wkclz.micro.dag.bean.req.ProcessVersionUpdateReq;
import com.wkclz.micro.dag.core.DagValidator;
import com.wkclz.micro.dag.mapper.DagProcessVersionMapper;
import com.wkclz.micro.dag.mapper.DagProcessMapper;
import com.wkclz.micro.dag.mapper.DagEdgeMapper;
import com.wkclz.micro.dag.mapper.DagNodeMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class DagProcessVersionServiceTest {

    private DagProcessVersionService service;
    private DagProcessVersionMapper mapper;
    private DagProcessMapper processMapper;
    private DagNodeMapper nodeMapper;
    private DagEdgeMapper edgeMapper;
    private NodeHandlerRegistry registry;

    @BeforeEach
    void setUp() {
        mapper = mock(DagProcessVersionMapper.class);
        processMapper = mock(DagProcessMapper.class);
        nodeMapper = mock(DagNodeMapper.class);
        edgeMapper = mock(DagEdgeMapper.class);
        registry = mock(NodeHandlerRegistry.class);
        // 含系统 handler（system-start/system-end 永久注册，特殊节点带 handler 时需已注册）
        when(registry.codes()).thenReturn(Set.of("h1", "system-start", "system-end"));
        // 规则 9 按 handler.paramSchema 校验 params：占位 handler 无 schema，保证既有用例不被参数校验拦截
        when(registry.get(any(String.class))).thenAnswer(inv -> new NodeHandler() {
            @Override
            public String nodeCode() { return String.valueOf(inv.getArgument(0)); }
            @Override
            public NodeResult execute(NodeContext ctx) { return NodeResult.ok(Map.of()); }
        });
        service = new DagProcessVersionService(new DagValidator(), registry, nodeMapper, edgeMapper, processMapper);
        ReflectionTestUtils.setField(service, "mapper", mapper);
    }

    private DagProcessVersion version() {
        DagProcessVersion d = new DagProcessVersion();
        d.setId(1L);
        d.setDagCode("d1");
        d.setDagVersion(0);
        d.setStatus("DRAFT");
        return d;
    }

    private DagProcess process() {
        DagProcess p = new DagProcess();
        p.setDagCode("d1");
        p.setDagName("旧名");
        return p;
    }

    private DagNode nodeRow(String code, String nodeType, String handler) {
        DagNode row = new DagNode();
        row.setDagCode("d1");
        row.setDagVersion(0);
        row.setNodeCode(code);
        row.setNodeName(code);
        row.setNodeType(nodeType);
        row.setHandler(handler);
        return row;
    }

    private DagEdge edgeRow(String from, String to) {
        DagEdge e = new DagEdge();
        e.setDagCode("d1");
        e.setDagVersion(0);
        e.setEdgeCode(from + "->" + to);
        e.setFromCode(from);
        e.setToCode(to);
        return e;
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

    private ProcessVersionCreateReq createReq(String dagCode, String dagName) {
        ProcessVersionCreateReq req = new ProcessVersionCreateReq();
        req.setDagCode(dagCode);
        req.setDagName(dagName);
        // 定义需满足入口/汇点约束：唯一 START 入口、唯一 END 汇点
        req.setNodes(List.of(node("start", NodeType.START.getCode(), null), node("n1", null, "h1"),
            node("end", NodeType.END.getCode(), null)));
        req.setEdges(List.of(edge("start", "n1"), edge("n1", "end")));
        return req;
    }

    @Test
    void publish_notDraft_throws() {
        DagProcessVersion d = version();
        d.setStatus("PUBLISHED");
        when(mapper.selectById(1L)).thenReturn(d);
        assertThrows(ValidationException.class, () -> service.publish(1L));
    }

    @Test
    void publish_success_versionIncrement() {
        DagProcessVersion d = version();
        when(mapper.selectById(1L)).thenReturn(d);
        when(mapper.selectOneByEntity(any())).thenReturn(d);
        when(nodeMapper.selectByEntity(any())).thenReturn(List.of(
            nodeRow("start", NodeType.START.getCode(), null), nodeRow("a", null, "h1"),
            nodeRow("end", NodeType.END.getCode(), null)));
        when(edgeMapper.selectByEntity(any())).thenReturn(List.of(edgeRow("start", "a"), edgeRow("a", "end")));
        DagProcessVersion existing = new DagProcessVersion();
        existing.setDagVersion(1);
        when(mapper.selectByEntity(any())).thenReturn(List.of(existing));

        Integer v = service.publish(1L);

        assertEquals(2, v);
        assertEquals("PUBLISHED", d.getStatus());
        // 节点/边行随发布版本重挂，新版本可 load（否则发布后设计详情报"版本无节点"）
        ArgumentCaptor<DagNode> nodeCaptor = ArgumentCaptor.forClass(DagNode.class);
        verify(nodeMapper, times(3)).insert(nodeCaptor.capture());
        for (DagNode row : nodeCaptor.getAllValues()) {
            assertEquals(2, row.getDagVersion());
        }
    }

    @Test
    void createDraftOk() {
        when(processMapper.selectOneByEntity(any())).thenReturn(process());
        // insert 回填 id（BaseService.insert 依赖 mapper.insert，mock 场景需手动回填）
        when(mapper.insert(any(DagProcessVersion.class))).thenAnswer(inv -> {
            DagProcessVersion v = inv.getArgument(0);
            v.setId(200L);
            return 1;
        });
        // rebuildNodesAndEdges 先删后插：删除前的查询返回空列表
        when(nodeMapper.selectByEntity(any())).thenReturn(List.of());
        when(edgeMapper.selectByEntity(any())).thenReturn(List.of());

        Long id = service.createDraft(createReq("d1", "新名"));

        assertNotNull(id);
        ArgumentCaptor<DagProcessVersion> cap = ArgumentCaptor.forClass(DagProcessVersion.class);
        verify(mapper).insert(cap.capture());
        assertEquals(0, cap.getValue().getDagVersion());
        assertEquals(ProcessStatus.DRAFT.name(), cap.getValue().getStatus());
        // 名称同步主表
        ArgumentCaptor<DagProcess> pCap = ArgumentCaptor.forClass(DagProcess.class);
        verify(processMapper).updateById(pCap.capture());
        assertEquals("新名", pCap.getValue().getDagName());
        verify(nodeMapper, times(3)).insert(any());
    }

    @Test
    void createDraftDuplicateBlocked() {
        when(processMapper.selectOneByEntity(any())).thenReturn(process());
        when(mapper.insert(any(DagProcessVersion.class)))
            .thenThrow(new DuplicateKeyException("uk_code_version"));

        ValidationException e = assertThrows(ValidationException.class,
            () -> service.createDraft(createReq("d1", "名")));
        assertTrue(e.getMessage().contains("已有编辑中草稿"));
    }

    @Test
    void createDraftProcessMissing() {
        when(processMapper.selectOneByEntity(any())).thenReturn(null);

        ValidationException e = assertThrows(ValidationException.class,
            () -> service.createDraft(createReq("dag_none", "名")));
        assertTrue(e.getMessage().contains("流程不存在"));
    }

    @Test
    void updateDraftRejectsPublished() {
        DagProcessVersion v = version();
        v.setId(9L);
        v.setDagVersion(2);
        v.setStatus(ProcessStatus.PUBLISHED.name());
        when(mapper.selectById(9L)).thenReturn(v);

        ProcessVersionUpdateReq req = new ProcessVersionUpdateReq();
        req.setId(9L);
        req.setDagName("名");
        req.setNodes(List.of(node("start", NodeType.START.getCode(), null), node("n1", null, "h1"),
            node("end", NodeType.END.getCode(), null)));
        req.setEdges(List.of(edge("start", "n1"), edge("n1", "end")));

        ValidationException e = assertThrows(ValidationException.class, () -> service.updateDraft(req));
        assertTrue(e.getMessage().contains("仅 DRAFT 版本可修改"));
    }
}
