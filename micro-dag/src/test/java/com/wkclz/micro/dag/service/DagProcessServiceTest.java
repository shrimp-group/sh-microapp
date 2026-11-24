package com.wkclz.micro.dag.service;

import com.wkclz.core.exception.ValidationException;
import com.wkclz.micro.dag.bean.entity.DagProcess;
import com.wkclz.micro.dag.bean.entity.DagProcessVersion;
import com.wkclz.micro.dag.bean.enums.ProcessStatus;
import com.wkclz.micro.dag.bean.req.ProcessCreateReq;
import com.wkclz.micro.dag.bean.resp.ProcessCreateResp;
import com.wkclz.micro.dag.bean.resp.ProcessPageResp;
import com.wkclz.micro.dag.mapper.DagProcessMapper;
import com.wkclz.micro.dag.mapper.DagProcessVersionMapper;
import com.wkclz.redis.helper.RedisIdGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class DagProcessServiceTest {

    private DagProcessService service;
    private DagProcessMapper processMapper;
    private DagProcessVersionMapper versionMapper;
    private DagProcessVersionService versionService;
    private RedisIdGenerator idGenerator;

    @BeforeEach
    void setUp() {
        processMapper = mock(DagProcessMapper.class);
        versionMapper = mock(DagProcessVersionMapper.class);
        versionService = mock(DagProcessVersionService.class);
        idGenerator = mock(RedisIdGenerator.class);
        service = new DagProcessService();
        ReflectionTestUtils.setField(service, "versionService", versionService);
        ReflectionTestUtils.setField(service, "versionMapper", versionMapper);
        ReflectionTestUtils.setField(service, "idGenerator", idGenerator);
        ReflectionTestUtils.setField(service, "mapper", processMapper);
    }

    @Test
    void createOk() {
        when(idGenerator.generateBase32IdWithPrefix("dag_")).thenReturn("dag_new1");
        // insert 回填 id（BaseService.insert 依赖 mapper.insert，mock 场景需手动回填）
        when(processMapper.insert(any(DagProcess.class))).thenAnswer(inv -> {
            DagProcess p = inv.getArgument(0);
            p.setId(100L);
            return 1;
        });

        ProcessCreateReq req = new ProcessCreateReq();
        req.setDagName("流程A");
        req.setNodes(List.of());
        req.setEdges(List.of());

        ProcessCreateResp resp = service.create(req);

        assertEquals(100L, resp.getId());
        assertEquals("dag_new1", resp.getDagCode());
        ArgumentCaptor<DagProcess> cap = ArgumentCaptor.forClass(DagProcess.class);
        verify(processMapper).insert(cap.capture());
        assertEquals("流程A", cap.getValue().getDagName());
        // v0 草稿由版本域统一创建
        ArgumentCaptor<com.wkclz.micro.dag.bean.req.ProcessVersionCreateReq> vCap =
            ArgumentCaptor.forClass(com.wkclz.micro.dag.bean.req.ProcessVersionCreateReq.class);
        verify(versionService).createDraft(vCap.capture());
        assertEquals("dag_new1", vCap.getValue().getDagCode());
        assertEquals("流程A", vCap.getValue().getDagName());
    }

    @Test
    void createNameRequired() {
        ProcessCreateReq req = new ProcessCreateReq();
        req.setDagName(" ");

        ValidationException e = assertThrows(ValidationException.class, () -> service.create(req));
        assertEquals("dagName 不能为空", e.getMessage());
    }

    @Test
    void pageAggregatesVersionStatus() {
        // 分页返回一条流程
        DagProcess p = new DagProcess();
        p.setId(1L);
        p.setDagCode("dag_p1");
        p.setDagName("流程A");
        // BaseService.selectPage 内部调用 selectCountByEntity + selectByEntityWithLimit（mapper 层无 selectPage）
        when(processMapper.selectCountByEntity(any(DagProcess.class))).thenReturn(1L);
        when(processMapper.selectByEntityWithLimit(any(DagProcess.class))).thenReturn(List.of(p));
        // 版本表：一条 PUBLISHED v2 + 一条 DRAFT v0
        DagProcessVersion pub = new DagProcessVersion();
        pub.setId(11L);
        pub.setDagCode("dag_p1");
        pub.setDagVersion(2);
        pub.setStatus(ProcessStatus.PUBLISHED.name());
        DagProcessVersion draft = new DagProcessVersion();
        draft.setId(12L);
        draft.setDagCode("dag_p1");
        draft.setDagVersion(0);
        draft.setStatus(ProcessStatus.DRAFT.name());
        when(versionMapper.selectByEntity(any(DagProcessVersion.class))).thenReturn(List.of(pub, draft));
        com.wkclz.core.base.PageData<ProcessPageResp> resp = service.page(new DagProcess());

        assertEquals(1, resp.getRecords().size());
        ProcessPageResp row = resp.getRecords().get(0);
        assertEquals("EDITING", row.getStatus());
        assertEquals(12L, row.getDraftId());
        assertEquals(2, row.getLatestVersion());
        assertEquals(11L, row.getLatestPublishedId());
    }

    @Test
    void pagePublishedWhenNoDraft() {
        DagProcess p = new DagProcess();
        p.setId(1L);
        p.setDagCode("dag_p1");
        p.setDagName("流程A");
        when(processMapper.selectCountByEntity(any(DagProcess.class))).thenReturn(1L);
        when(processMapper.selectByEntityWithLimit(any(DagProcess.class))).thenReturn(List.of(p));
        DagProcessVersion pub = new DagProcessVersion();
        pub.setId(11L);
        pub.setDagCode("dag_p1");
        pub.setDagVersion(2);
        pub.setStatus(ProcessStatus.PUBLISHED.name());
        when(versionMapper.selectByEntity(any(DagProcessVersion.class))).thenReturn(List.of(pub));

        com.wkclz.core.base.PageData<ProcessPageResp> resp = service.page(new DagProcess());

        assertEquals("PUBLISHED", resp.getRecords().get(0).getStatus());
        assertNull(resp.getRecords().get(0).getDraftId());
        assertEquals(2, resp.getRecords().get(0).getLatestVersion());
    }
}
