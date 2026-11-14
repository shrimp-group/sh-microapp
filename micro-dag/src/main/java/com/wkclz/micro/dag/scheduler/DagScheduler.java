package com.wkclz.micro.dag.scheduler;

import com.wkclz.micro.dag.bean.dto.DagDefModel;
import com.wkclz.micro.dag.bean.entity.DagInstance;
import com.wkclz.micro.dag.bean.entity.DagNodeInstance;
import com.wkclz.micro.dag.bean.enums.ExecutionEvent;
import com.wkclz.micro.dag.bean.enums.NodeStatus;
import com.wkclz.micro.dag.config.DagProperties;
import com.wkclz.micro.dag.core.DagReadyCalculator;
import com.wkclz.micro.dag.mapper.DagInstanceMapper;
import com.wkclz.micro.dag.mapper.DagNodeInstanceMapper;
import com.wkclz.micro.dag.service.DagDefinitionService;
import com.wkclz.micro.dag.service.DagExecutionLogService;
import com.wkclz.micro.dag.worker.DagWorkerExecutor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.lang.management.ManagementFactory;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 调度循环（双通道的"轮询兜底"侧，亦是就绪领取的唯一 CAS 入口）：
 * promoteRetrying → reclaimExpiredLease → 逐运行实例计算就绪 → batchSize 限流 CAS 领取 → worker.submit
 */
@Slf4j
@Component
@ConditionalOnProperty(prefix = "dag", name = "enabled", havingValue = "true", matchIfMissing = true)
public class DagScheduler {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final long LEASE_MARGIN_MS = 30_000;
    private static final String WORKER_ID = ManagementFactory.getRuntimeMXBean().getName();

    private final DagInstanceMapper instanceMapper;
    private final DagNodeInstanceMapper nodeMapper;
    private final DagDefinitionService definitionService;
    private final DagExecutionLogService logService;
    private final DagReadyCalculator readyCalculator;
    private final DagWorkerExecutor worker;
    private final DagProperties properties;

    public DagScheduler(DagInstanceMapper instanceMapper, DagNodeInstanceMapper nodeMapper,
                        DagDefinitionService definitionService, DagExecutionLogService logService,
                        DagReadyCalculator readyCalculator, DagWorkerExecutor worker,
                        DagProperties properties) {
        log.info("micro-dag: 初始化调度器 worker={} channel={}", WORKER_ID, properties.getChannel());
        this.instanceMapper = instanceMapper;
        this.nodeMapper = nodeMapper;
        this.definitionService = definitionService;
        this.logService = logService;
        this.readyCalculator = readyCalculator;
        this.worker = worker;
        this.properties = properties;
    }

    /** 轮询兜底通道；pubsub-only 模式不轮询 */
    @Scheduled(fixedDelayString = "${dag.scheduler.interval-ms:5000}")
    public void tick() {
        if ("pubsub".equalsIgnoreCase(properties.getChannel())) {
            return;
        }
        scan();
    }

    /** 事件唤醒通道（DagWakeupListener 调用）；db-only 模式忽略事件 */
    public void wakeUp() {
        log.info("micro-dag: 收到调度唤醒");
        if ("db".equalsIgnoreCase(properties.getChannel())) {
            return;
        }
        scan();
    }

    synchronized void scan() {
        log.info("micro-dag: 调度扫描开始");
        LocalDateTime now = LocalDateTime.now();
        nodeMapper.promoteRetrying(now);
        reclaimExpired(now);
        List<DagInstance> runningInstances = instanceMapper.listRunning();
        for (DagInstance instance : runningInstances) {
            try {
                submitReadyNodes(instance, now);
            } catch (Exception e) {
                log.error("micro-dag: 实例调度异常 instanceCode={}", instance.getInstanceCode(), e);
            }
        }
        log.info("micro-dag: 调度扫描结束 runningInstances={}", runningInstances.size());
    }

    private void reclaimExpired(LocalDateTime now) {
        List<DagNodeInstance> expired = nodeMapper.listExpiredLease(now);
        if (expired.isEmpty()) {
            return;
        }
        int count = nodeMapper.reclaimExpiredLease(now);
        for (DagNodeInstance node : expired) {
            logService.log(node.getInstanceCode(), node.getNodeCode(), node.getAttempt(),
                ExecutionEvent.LEASE_RECLAIM, null, node.getLeaseOwner());
        }
        log.info("micro-dag: 租约回收 {} 个节点", count);
    }

    private void submitReadyNodes(DagInstance instance, LocalDateTime now) {
        List<DagNodeInstance> nodes = nodeMapper.listByInstanceCode(instance.getInstanceCode());
        DagDefModel def = definitionService.load(instance.getDagCode(), instance.getDagVersion());
        Map<String, NodeStatus> statusMap = new HashMap<>();
        Map<String, Map<String, Object>> outputs = new HashMap<>();
        for (DagNodeInstance n : nodes) {
            statusMap.put(n.getNodeCode(), NodeStatus.valueOf(n.getStatus()));
            outputs.put(n.getNodeCode(), parseMap(n.getOutputJson()));
        }
        Map<String, Object> ctx = parseMap(instance.getContextJson());

        Set<String> ready = readyCalculator.readyNodes(def, statusMap, ctx, outputs);
        int claimed = 0;
        int batchSize = properties.getScheduler().getBatchSize();
        for (DagNodeInstance node : nodes) {
            if (claimed >= batchSize) {
                break;
            }
            if (!ready.contains(node.getNodeCode())) {
                continue;
            }
            LocalDateTime expire = now.plusNanos(leaseDuration(node) * 1_000_000);
            int ok = nodeMapper.casClaim(node.getId(), node.getVersion(), WORKER_ID, expire);
            if (ok == 1) {
                claimed++;
                node.setStatus(NodeStatus.RUNNING.name());
                node.setLeaseOwner(WORKER_ID);
                node.setLeaseExpireTime(expire);
                node.setAttempt(node.getAttempt() == null ? 1 : node.getAttempt() + 1);
                logService.log(node.getInstanceCode(), node.getNodeCode(), node.getAttempt(),
                    ExecutionEvent.CLAIM, null, WORKER_ID);
                worker.submit(node, instance);
            }
        }
        if (claimed > 0) {
            log.info("micro-dag: 本轮领取 instanceCode={} claimed={}", instance.getInstanceCode(), claimed);
        }
    }

    /** 租约时长与 DagWorkerExecutor.leaseDuration 保持同一规则 */
    private long leaseDuration(DagNodeInstance node) {
        long fixedMs = properties.getLease().getFixedMs();
        if (node.getTimeoutMs() == null) {
            return fixedMs;
        }
        return Math.max(fixedMs, node.getTimeoutMs() + LEASE_MARGIN_MS);
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> parseMap(String json) {
        if (json == null || json.isBlank()) {
            return Map.of();
        }
        try {
            return OBJECT_MAPPER.readValue(json, Map.class);
        } catch (Exception e) {
            log.error("micro-dag: JSON 解析失败 json={}", json, e);
            return Map.of();
        }
    }
}
