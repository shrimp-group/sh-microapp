package com.wkclz.micro.dag.service;

import com.wkclz.core.base.PageData;
import com.wkclz.core.exception.ValidationException;
import com.wkclz.micro.dag.bean.constant.DagConstants;
import com.wkclz.micro.dag.bean.dto.DagDefModel;
import com.wkclz.micro.dag.bean.dto.EdgeDef;
import com.wkclz.micro.dag.bean.dto.NodeDef;
import com.wkclz.micro.dag.bean.entity.DagProcessVersion;
import com.wkclz.micro.dag.bean.entity.DagInstance;
import com.wkclz.micro.dag.bean.entity.DagNodeInstance;
import com.wkclz.micro.dag.bean.enums.NodeType;
import com.wkclz.micro.dag.bean.req.InstancePageReq;
import com.wkclz.micro.dag.bean.req.LaunchReq;
import com.wkclz.micro.dag.bean.resp.InstanceDagViewResp;
import com.wkclz.micro.dag.bean.resp.InstanceResp;
import com.wkclz.micro.dag.mapper.DagInstanceMapper;
import com.wkclz.micro.dag.mapper.DagNodeInstanceMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.test.util.ReflectionTestUtils;
import tools.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class DagInstanceServiceTest {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private DagInstanceService service;
    private DagInstanceMapper mapper;
    private DagNodeInstanceMapper nodeInstanceMapper;
    private DagProcessVersionService versionService;
    private DagExecutionLogService executionLogService;

    @BeforeEach
    void setUp() {
        mapper = mock(DagInstanceMapper.class);
        nodeInstanceMapper = mock(DagNodeInstanceMapper.class);
        versionService = mock(DagProcessVersionService.class);
        executionLogService = mock(DagExecutionLogService.class);
        service = new DagInstanceService();
        ReflectionTestUtils.setField(service, "versionService", versionService);
        ReflectionTestUtils.setField(service, "nodeInstanceMapper", nodeInstanceMapper);
        ReflectionTestUtils.setField(service, "executionLogService", executionLogService);
        ReflectionTestUtils.setField(service, "mapper", mapper);

        DagProcessVersion def = new DagProcessVersion();
        def.setDagCode("d1");
        def.setDagVersion(3);
        def.setStatus("PUBLISHED");
        when(versionService.selectByEntity(any())).thenReturn(List.of(def));
        when(versionService.load("d1", 3)).thenReturn(defModel());
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
        ab.setType(DagConstants.EDGE_TYPE_NORMAL);
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
    void launch_branchNode_snapshotsConditionEdgesIntoInput() {
        when(mapper.selectCountByEntity(any())).thenReturn(0L);
        when(versionService.load("d1", 3)).thenReturn(branchDefModel());
        LaunchReq req = launchReq();
        // 实例参数携带与分支目标同名的 key，验证不可覆盖 branches 系统键
        req.setParams(Map.of("sourceType", "api", "x", "override"));

        service.launch(req);

        ArgumentCaptor<List<DagNodeInstance>> nodeCap = ArgumentCaptor.captor();
        verify(nodeInstanceMapper).insertBatch(nodeCap.capture());
        DagNodeInstance br = nodeCap.getValue().stream()
            .filter(n -> "br".equals(n.getNodeCode())).findFirst().orElseThrow();
        Map<String, Object> input = parseMap(br.getInputParamsJson());
        // BRANCH 出边条件按 toCode 快照进输入参数
        assertEquals("${ctx.sourceType} == 'x'", input.get("x"));
        assertEquals("${ctx.sourceType} == 'y'", input.get("y"));
        assertEquals("api", input.get("sourceType"));
        // 非 BRANCH 节点不携带分支快照，实例参数保持原样
        DagNodeInstance x = nodeCap.getValue().stream()
            .filter(n -> "x".equals(n.getNodeCode())).findFirst().orElseThrow();
        assertEquals("override", parseMap(x.getInputParamsJson()).get("x"));
    }

    /** 组装 d1 v3 的分支定义：br --条件边--> x / y */
    private DagDefModel branchDefModel() {
        NodeDef br = new NodeDef();
        br.setCode("br");
        br.setName("br");
        br.setNodeType(NodeType.BRANCH.getCode());
        NodeDef x = new NodeDef();
        x.setCode("x");
        x.setName("x");
        x.setHandler("h1");
        NodeDef y = new NodeDef();
        y.setCode("y");
        y.setName("y");
        y.setHandler("h1");
        EdgeDef ex = new EdgeDef();
        ex.setEdgeCode("br->x");
        ex.setFromCode("br");
        ex.setToCode("x");
        ex.setType(DagConstants.EDGE_TYPE_CONDITION);
        ex.setWhenExpr("${ctx.sourceType} == 'x'");
        EdgeDef ey = new EdgeDef();
        ey.setEdgeCode("br->y");
        ey.setFromCode("br");
        ey.setToCode("y");
        ey.setType(DagConstants.EDGE_TYPE_CONDITION);
        ey.setWhenExpr("${ctx.sourceType} == 'y'");
        DagDefModel def = new DagDefModel();
        def.setDagCode("d1");
        def.setDagVersion(3);
        def.setNodes(List.of(br, x, y));
        def.setEdges(List.of(ex, ey));
        return def;
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> parseMap(String json) {
        return OBJECT_MAPPER.readValue(json, Map.class);
    }

    @Test
    void launch_timerNode_snapshotsUpstreamsIntoInput() {
        when(mapper.selectCountByEntity(any())).thenReturn(0L);
        when(versionService.load("d1", 3)).thenReturn(timerDefModel());
        LaunchReq req = launchReq();
        // 实例参数携带与快照键同名的 key，验证不可覆盖 upstreams 系统键
        req.setParams(Map.of("sourceType", "api", "upstreams", "override"));

        service.launch(req);

        ArgumentCaptor<List<DagNodeInstance>> nodeCap = ArgumentCaptor.captor();
        verify(nodeInstanceMapper).insertBatch(nodeCap.capture());
        DagNodeInstance t = nodeCap.getValue().stream()
            .filter(n -> "t".equals(n.getNodeCode())).findFirst().orElseThrow();
        Map<String, Object> input = parseMap(t.getInputParamsJson());
        // 入边 fromCode（NORMAL+CONDITION）去重快照，实例参数不可覆盖
        assertEquals(List.of("a", "b"), input.get("upstreams"));
        assertEquals("api", input.get("sourceType"));
        // 非 timer 节点不写入 upstreams 快照（该键仅来自实例参数透传，保持原值）
        DagNodeInstance a = nodeCap.getValue().stream()
            .filter(n -> "a".equals(n.getNodeCode())).findFirst().orElseThrow();
        assertEquals("override", parseMap(a.getInputParamsJson()).get("upstreams"));
    }

    /** 组装 d1 v3 的定时器定义：a --普通边--> t 与 a --条件边--> t（重复上游验证去重）、b --条件边--> t */
    private DagDefModel timerDefModel() {
        NodeDef a = new NodeDef();
        a.setCode("a");
        a.setName("a");
        a.setHandler("h1");
        NodeDef b = new NodeDef();
        b.setCode("b");
        b.setName("b");
        b.setHandler("h1");
        NodeDef t = new NodeDef();
        t.setCode("t");
        t.setName("t");
        t.setHandler("system-timer");
        EdgeDef at = new EdgeDef();
        at.setEdgeCode("a->t");
        at.setFromCode("a");
        at.setToCode("t");
        at.setType(DagConstants.EDGE_TYPE_NORMAL);
        EdgeDef at2 = new EdgeDef();
        at2.setEdgeCode("a->t-cond");
        at2.setFromCode("a");
        at2.setToCode("t");
        at2.setType(DagConstants.EDGE_TYPE_CONDITION);
        EdgeDef bt = new EdgeDef();
        bt.setEdgeCode("b->t");
        bt.setFromCode("b");
        bt.setToCode("t");
        bt.setType(DagConstants.EDGE_TYPE_CONDITION);
        bt.setWhenExpr("${ctx.sourceType} == 'x'");
        DagDefModel def = new DagDefModel();
        def.setDagCode("d1");
        def.setDagVersion(3);
        def.setNodes(List.of(a, b, t));
        def.setEdges(List.of(at, at2, bt));
        return def;
    }

    @Test
    void launch_noPublishedDefinition_throws() {
        when(mapper.selectCountByEntity(any())).thenReturn(0L);
        when(versionService.selectByEntity(any())).thenReturn(List.of());
        assertThrows(ValidationException.class, () -> service.launch(launchReq()));
    }

    @Test
    void page_mapsToResp() {
        DagInstanceService spyService = Mockito.spy(service);
        DagInstance inst = new DagInstance();
        inst.setInstanceCode("INST001");
        inst.setDagCode("d1");
        inst.setDagVersion(3);
        inst.setStatus("RUNNING");
        doReturn(PageData.of(List.of(inst), 1L, 1L, 10L)).when(spyService).selectPage(any());

        InstancePageReq req = new InstancePageReq();
        req.setStatus("RUNNING");
        PageData<InstanceResp> result = spyService.page(req);

        assertEquals(1, result.getRecords().size());
        assertEquals("INST001", result.getRecords().get(0).getInstanceCode());
        assertEquals("d1", result.getRecords().get(0).getDagCode());
        assertEquals(1L, result.getTotal());
    }

    @Test
    void detail_assemblesDagView() {
        DagInstance inst = new DagInstance();
        inst.setInstanceCode("INST001");
        inst.setDagCode("d1");
        inst.setDagVersion(3);
        inst.setStatus("RUNNING");
        inst.setContextJson("{\"t\":\"api\"}");
        when(mapper.selectOneByEntity(any())).thenReturn(inst);

        DagDefModel def = new DagDefModel();
        NodeDef na = new NodeDef();
        na.setCode("a");
        na.setPosX(10);
        na.setPosY(20);
        def.setNodes(List.of(na));
        EdgeDef e = new EdgeDef();
        e.setFromCode("a");
        e.setToCode("b");
        def.setEdges(List.of(e));
        when(versionService.load("d1", 3)).thenReturn(def);

        DagNodeInstance nodeA = new DagNodeInstance();
        nodeA.setInstanceCode("INST001");
        nodeA.setNodeCode("a");
        nodeA.setStatus("RUNNING");
        nodeA.setOutputJson("{\"rows\":1}");
        when(nodeInstanceMapper.listByInstanceCode("INST001")).thenReturn(List.of(nodeA));

        InstanceDagViewResp view = service.detail("INST001");

        assertEquals("INST001", view.getInstance().getInstanceCode());
        assertEquals(1, view.getNodes().size());
        assertEquals("RUNNING", view.getNodes().get(0).getStatus());
        assertEquals(Integer.valueOf(10), view.getNodes().get(0).getPosX());
        assertEquals(Integer.valueOf(20), view.getNodes().get(0).getPosY());
        assertEquals("b", view.getEdges().get(0).getToCode());
        assertEquals(1, view.getNodeOutputs().get("a").get("rows"));
        assertEquals("api", view.getCtx().get("t"));
    }

    @Test
    void detail_notExist_throws() {
        when(mapper.selectOneByEntity(any())).thenReturn(null);
        assertThrows(ValidationException.class, () -> service.detail("INST404"));
    }
}
