package com.wkclz.micro.dag.scheduler;

import com.wkclz.core.exception.ValidationException;
import com.wkclz.micro.dag.bean.dto.DagDefModel;
import com.wkclz.micro.dag.bean.dto.NodeDef;
import com.wkclz.micro.dag.bean.entity.DagProcessVersion;
import com.wkclz.micro.dag.bean.enums.NodeType;
import com.wkclz.micro.dag.bean.req.LaunchReq;
import com.wkclz.micro.dag.config.DagProperties;
import com.wkclz.micro.dag.service.DagProcessVersionService;
import com.wkclz.micro.dag.service.DagInstanceService;
import com.wkclz.redis.helper.RedisLock;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class DagTriggerSchedulerTest {

    private static final DateTimeFormatter FIRE_TIME = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    private DagProcessVersionService versionService;
    private DagInstanceService dagInstanceService;
    private DagProperties properties;
    private RedisLock redisLock;
    private ObjectProvider<RedisLock> redisLockProvider;
    private DagTriggerScheduler scheduler;

    @BeforeEach
    @SuppressWarnings("unchecked")
    void setUp() {
        versionService = mock(DagProcessVersionService.class);
        dagInstanceService = mock(DagInstanceService.class);
        properties = new DagProperties();
        redisLock = mock(RedisLock.class);
        when(redisLock.tryLock(anyString(), anyLong(), any())).thenReturn("req-1");
        redisLockProvider = (ObjectProvider<RedisLock>) mock(ObjectProvider.class);
        when(redisLockProvider.getIfAvailable()).thenReturn(redisLock);
        scheduler = buildScheduler();
    }

    /** @Resource 字段注入的手工装配 */
    private DagTriggerScheduler buildScheduler() {
        DagTriggerScheduler scheduler = new DagTriggerScheduler();
        ReflectionTestUtils.setField(scheduler, "versionService", versionService);
        ReflectionTestUtils.setField(scheduler, "dagInstanceService", dagInstanceService);
        ReflectionTestUtils.setField(scheduler, "properties", properties);
        ReflectionTestUtils.setField(scheduler, "redisLockProvider", redisLockProvider);
        return scheduler;
    }

    @Test
    void tick_timerDue_launchesWithTraceCodeAndParams() {
        when(versionService.selectByEntity(any())).thenReturn(List.of(meta("d1", 1)));
        when(versionService.load("d1", 1)).thenReturn(timerDef("d1", 1, "* * * * * *"));

        LocalDateTime t0 = LocalDateTime.now().withNano(0);
        scheduler.scan(t0, redisLock);
        // 建状态轮次：nextFire 指向未来，不触发
        verify(dagInstanceService, never()).launch(any());

        scheduler.scan(t0.plusSeconds(2), redisLock);

        ArgumentCaptor<LaunchReq> captor = ArgumentCaptor.forClass(LaunchReq.class);
        verify(dagInstanceService).launch(captor.capture());
        LaunchReq req = captor.getValue();
        assertEquals("d1", req.getDagCode());
        // traceCode 含 TIMER 前缀与触发时间（到期槽位 t0+1s）
        assertEquals("TIMER-d1-v1-" + t0.plusSeconds(1).format(FIRE_TIME), req.getTraceCode());
        assertEquals("v1", req.getParams().get("bizKey"));
    }

    @Test
    void tick_duplicateTraceCode_idempotentNoThrow() {
        when(versionService.selectByEntity(any())).thenReturn(List.of(meta("d1", 1)));
        when(versionService.load("d1", 1)).thenReturn(timerDef("d1", 1, "* * * * * *"));
        when(dagInstanceService.launch(any())).thenReturn(1L)
            .thenThrow(ValidationException.of("traceCode 已存在: TIMER-d1-v1-x"));

        DagTriggerScheduler peer = buildScheduler();
        LocalDateTime t0 = LocalDateTime.now().withNano(0);
        // 模拟集群两节点扫描同一到期槽位：后触发者被 launch 幂等校验拦截，不外抛
        assertDoesNotThrow(() -> {
            scheduler.scan(t0, redisLock);
            scheduler.scan(t0.plusSeconds(2), redisLock);
            peer.scan(t0, redisLock);
            peer.scan(t0.plusSeconds(2), redisLock);
        });
        verify(dagInstanceService, times(2)).launch(any());
    }

    @Test
    void tick_lockNotAcquired_skipsLaunch() {
        when(redisLock.tryLock(anyString(), anyLong(), any())).thenReturn(null);
        when(versionService.selectByEntity(any())).thenReturn(List.of(meta("d1", 1)));
        when(versionService.load("d1", 1)).thenReturn(timerDef("d1", 1, "* * * * * *"));

        LocalDateTime t0 = LocalDateTime.now().withNano(0);
        scheduler.scan(t0, redisLock);
        scheduler.scan(t0.plusSeconds(2), redisLock);

        verify(redisLock).tryLock(anyString(), anyLong(), any());
        verify(dagInstanceService, never()).launch(any());
    }

    @Test
    void tick_versionChanged_rebuildsStateAndReloads() {
        when(versionService.selectByEntity(any()))
            .thenReturn(List.of(meta("d1", 1)))
            .thenReturn(List.of(meta("d1", 1), meta("d1", 2)));
        when(versionService.load("d1", 1)).thenReturn(timerDef("d1", 1, "0 0 * * * *"));
        when(versionService.load("d1", 2)).thenReturn(timerDef("d1", 2, "0 0 * * * *"));

        LocalDateTime t0 = LocalDateTime.now().withNano(0);
        scheduler.scan(t0, redisLock);
        scheduler.scan(t0.plusSeconds(5), redisLock);

        verify(versionService).load("d1", 1);
        verify(versionService).load("d1", 2);
    }

    @Test
    void tick_triggerDisabled_noScan() {
        properties.getTrigger().setEnabled(false);

        scheduler.tick();

        verify(versionService, never()).selectByEntity(any());
        verify(dagInstanceService, never()).launch(any());
    }

    @Test
    void tick_noTimerTrigger_noLaunch() {
        when(versionService.selectByEntity(any())).thenReturn(List.of(meta("d1", 1)));
        NodeDef start = new NodeDef();
        start.setCode("start");
        start.setNodeType(NodeType.START.getCode());
        start.setParams(Map.of("foo", "bar"));
        DagDefModel def = new DagDefModel();
        def.setDagCode("d1");
        def.setDagVersion(1);
        def.setNodes(List.of(start));
        when(versionService.load("d1", 1)).thenReturn(def);

        LocalDateTime t0 = LocalDateTime.now().withNano(0);
        scheduler.scan(t0, redisLock);
        scheduler.scan(t0.plusSeconds(2), redisLock);

        verify(dagInstanceService, never()).launch(any());
    }

    private DagProcessVersion meta(String dagCode, int version) {
        DagProcessVersion d = new DagProcessVersion();
        d.setDagCode(dagCode);
        d.setDagVersion(version);
        d.setStatus("PUBLISHED");
        return d;
    }

    private DagDefModel timerDef(String dagCode, int version, String cron) {
        NodeDef start = new NodeDef();
        start.setCode("start");
        start.setNodeType(NodeType.START.getCode());
        start.setParams(Map.of("triggerType", "TIMER", "cron", cron,
            "triggerParams", Map.of("bizKey", "v" + version)));
        DagDefModel def = new DagDefModel();
        def.setDagCode(dagCode);
        def.setDagVersion(version);
        def.setNodes(List.of(start));
        return def;
    }
}
