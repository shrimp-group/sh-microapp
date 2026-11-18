package com.wkclz.micro.dag.scheduler;

import com.wkclz.core.exception.ValidationException;
import com.wkclz.micro.dag.bean.dto.DagDefModel;
import com.wkclz.micro.dag.bean.dto.NodeDef;
import com.wkclz.micro.dag.bean.entity.DagDefinition;
import com.wkclz.micro.dag.bean.enums.DefinitionStatus;
import com.wkclz.micro.dag.bean.enums.NodeType;
import com.wkclz.micro.dag.bean.req.LaunchReq;
import com.wkclz.micro.dag.config.DagProperties;
import com.wkclz.micro.dag.service.DagDefinitionService;
import com.wkclz.micro.dag.service.DagInstanceService;
import com.wkclz.redis.helper.RedisLock;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.support.CronExpression;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * START 节点时间驱动触发器：轮询 PUBLISHED 定义，对配置了 triggerType=TIMER + cron 的 START 节点
 * 到点自动 launch 实例（traceCode 以触发时间做幂等键，Redis 锁防集群竞争）
 */
@Slf4j
@Component
@ConditionalOnProperty(prefix = "dag", name = "enabled", havingValue = "true", matchIfMissing = true)
public class DagTriggerScheduler {

    private static final String TRIGGER_TYPE_TIMER = "TIMER";
    private static final DateTimeFormatter FIRE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    private final DagDefinitionService definitionService;
    private final DagInstanceService dagInstanceService;
    private final DagProperties properties;
    /** RedisLock 由 sh-redis @Component 装配，无 Redis 环境可能缺失，缺失时触发跳过不崩 */
    private final ObjectProvider<RedisLock> redisLockProvider;

    /** 内存触发状态：dagCode → 触发状态 */
    private final Map<String, TriggerState> states = new HashMap<>();

    public DagTriggerScheduler(DagDefinitionService definitionService, DagInstanceService dagInstanceService,
                               DagProperties properties, ObjectProvider<RedisLock> redisLockProvider) {
        log.info("micro-dag: 初始化时间驱动触发器 intervalMs={}", properties.getTrigger().getIntervalMs());
        this.definitionService = definitionService;
        this.dagInstanceService = dagInstanceService;
        this.properties = properties;
        this.redisLockProvider = redisLockProvider;
    }

    @Scheduled(fixedDelayString = "${dag.trigger.interval-ms:30000}")
    public void tick() {
        log.debug("micro-dag: 触发器扫描开始");
        if (!properties.getTrigger().isEnabled()) {
            return;
        }
        RedisLock redisLock = redisLockProvider.getIfAvailable();
        if (redisLock == null) {
            log.warn("micro-dag: RedisLock 缺失，本轮触发跳过");
            return;
        }
        scan(LocalDateTime.now(), redisLock);
    }

    /** 单轮扫描：重建变化定义的触发状态 → 到期触发 */
    synchronized void scan(LocalDateTime now, RedisLock redisLock) {
        Map<String, Integer> publishedVersions = publishedMaxVersions();
        for (Map.Entry<String, Integer> entry : publishedVersions.entrySet()) {
            String dagCode = entry.getKey();
            Integer dagVersion = entry.getValue();
            try {
                refreshState(dagCode, dagVersion, now);
                TriggerState state = states.get(dagCode);
                if (state != null && state.getNextFire() != null && !state.getNextFire().isAfter(now)) {
                    fire(dagCode, state, redisLock);
                    // 只补触发最近一个到期槽位：停机期间错过的历史槽位直接跳过，从当前时间重新起算
                    state.setNextFire(state.getCron().next(now));
                }
            } catch (Exception e) {
                log.error("micro-dag: 触发扫描异常 dagCode={} dagVersion={}", dagCode, dagVersion, e);
            }
        }
    }

    /** PUBLISHED 定义按 dagCode 取最大 dagVersion */
    private Map<String, Integer> publishedMaxVersions() {
        DagDefinition query = new DagDefinition();
        query.setStatus(DefinitionStatus.PUBLISHED.name());
        List<DagDefinition> defs = definitionService.selectByEntity(query);
        Map<String, Integer> versions = new HashMap<>();
        for (DagDefinition d : defs) {
            versions.merge(d.getDagCode(), d.getDagVersion() == null ? 0 : d.getDagVersion(), Math::max);
        }
        return versions;
    }

    /** 状态缺失或 dagVersion 变化时重建（取第一个 TIMER 触发的 START 节点）；无 TIMER 触发则移除状态 */
    private void refreshState(String dagCode, Integer dagVersion, LocalDateTime now) {
        TriggerState old = states.get(dagCode);
        if (old != null && old.getDagVersion().equals(dagVersion)) {
            return;
        }
        log.info("micro-dag: 重建触发状态 dagCode={} dagVersion={}", dagCode, dagVersion);
        states.remove(dagCode);
        DagDefModel def;
        try {
            def = definitionService.load(dagCode, dagVersion);
        } catch (Exception e) {
            log.error("micro-dag: 触发状态重建失败 dagCode={} dagVersion={}", dagCode, dagVersion, e);
            return;
        }
        for (NodeDef n : def.getNodes()) {
            if (!NodeType.START.getCode().equals(n.getNodeType()) || n.getParams() == null
                || !TRIGGER_TYPE_TIMER.equals(n.getParams().get("triggerType"))) {
                continue;
            }
            Object cronValue = n.getParams().get("cron");
            if (cronValue == null || cronValue.toString().isBlank()) {
                log.error("micro-dag: TIMER 触发缺少 cron dagCode={} dagVersion={} node={}", dagCode, dagVersion, n.getCode());
                continue;
            }
            try {
                CronExpression cron = CronExpression.parse(cronValue.toString());
                TriggerState state = new TriggerState();
                state.setDagVersion(dagVersion);
                state.setCron(cron);
                state.setNextFire(cron.next(now));
                state.setTriggerParams(triggerParamsOf(n));
                states.put(dagCode, state);
                log.info("micro-dag: TIMER 触发注册 dagCode={} dagVersion={} node={} cron={} nextFire={}",
                    dagCode, dagVersion, n.getCode(), cronValue, state.getNextFire());
                return;
            } catch (IllegalArgumentException e) {
                log.error("micro-dag: cron 非法 dagCode={} dagVersion={} cron={}", dagCode, dagVersion, cronValue, e);
            }
        }
        log.info("micro-dag: 定义无可用 TIMER 触发 dagCode={} dagVersion={}", dagCode, dagVersion);
    }

    /** START 节点 params 中的 triggerParams，缺失为空 Map */
    @SuppressWarnings("unchecked")
    private Map<String, Object> triggerParamsOf(NodeDef n) {
        Object triggerParams = n.getParams().get("triggerParams");
        return triggerParams instanceof Map ? (Map<String, Object>) triggerParams : Map.of();
    }

    /** 触发一个到期槽位：traceCode 含触发时间，launch 的 traceCode 唯一校验天然幂等；Redis 锁防集群竞争 */
    private void fire(String dagCode, TriggerState state, RedisLock redisLock) {
        String fireTimeKey = state.getNextFire().format(FIRE_TIME_FORMATTER);
        String traceCode = "TIMER-" + dagCode + "-v" + state.getDagVersion() + "-" + fireTimeKey;
        String lockKey = "dag:trigger:lock:" + dagCode + ":" + fireTimeKey;
        String requestId = redisLock.tryLock(lockKey, 15, TimeUnit.SECONDS);
        if (requestId == null) {
            log.info("micro-dag: 触发锁未获取，跳过 dagCode={} fireTime={}", dagCode, fireTimeKey);
            return;
        }
        try {
            LaunchReq req = new LaunchReq();
            req.setDagCode(dagCode);
            req.setTraceCode(traceCode);
            req.setParams(state.getTriggerParams());
            try {
                dagInstanceService.launch(req);
                log.info("micro-dag: 定时触发成功 dagCode={} traceCode={}", dagCode, traceCode);
            } catch (ValidationException e) {
                if (e.getMessage() != null && e.getMessage().contains("traceCode 已存在")) {
                    log.info("micro-dag: 定时触发幂等跳过 traceCode={}", traceCode);
                } else {
                    log.error("micro-dag: 定时触发失败 traceCode={}", traceCode, e);
                }
            }
        } finally {
            redisLock.releaseLock(lockKey, requestId);
        }
    }

    /** 单定义触发状态（内存态，实例重启即失效，下轮自动重建） */
    @Data
    static class TriggerState {
        private Integer dagVersion;
        private CronExpression cron;
        private LocalDateTime nextFire;
        private Map<String, Object> triggerParams;
    }
}
