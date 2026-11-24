package com.wkclz.micro.dag.scheduler;

import com.wkclz.micro.dag.bean.constant.DagConstants;
import com.wkclz.micro.dag.bean.dto.DagDefModel;
import com.wkclz.micro.dag.bean.dto.NodeInstanceLeaseDto;
import com.wkclz.micro.dag.bean.dto.NodeDef;
import com.wkclz.micro.dag.bean.entity.DagInstance;
import com.wkclz.micro.dag.bean.entity.DagNodeInstance;
import com.wkclz.micro.dag.bean.enums.ExecutionEvent;
import com.wkclz.micro.dag.bean.enums.NodeStatus;
import com.wkclz.micro.dag.bean.enums.NodeType;
import com.wkclz.micro.dag.config.DagProperties;
import com.wkclz.micro.dag.core.DagReadyCalculator;
import com.wkclz.micro.dag.helper.DagLeaseHelper;
import com.wkclz.micro.dag.mapper.DagInstanceMapper;
import com.wkclz.micro.dag.mapper.DagNodeInstanceMapper;
import com.wkclz.micro.dag.service.DagProcessVersionService;
import com.wkclz.micro.dag.service.DagEngineService;
import com.wkclz.micro.dag.service.DagExecutionLogService;
import com.wkclz.micro.dag.utils.JsonUtils;
import com.wkclz.micro.dag.utils.RuntimeUtils;
import com.wkclz.micro.dag.worker.DagWorkerExecutor;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

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

    private final String workerId = RuntimeUtils.workerId();

    @Resource
    private DagInstanceMapper instanceMapper;
    @Resource
    private DagNodeInstanceMapper nodeMapper;
    @Resource
    private DagProcessVersionService versionService;
    @Resource
    private DagExecutionLogService logService;
    @Resource
    private DagReadyCalculator readyCalculator;
    @Resource
    private DagWorkerExecutor worker;
    @Resource
    private DagEngineService engineService;
    @Resource
    private DagProperties properties;

    @PostConstruct
    public void init() {
        log.info("micro-dag: 初始化调度器 worker={} channel={}", workerId, properties.getChannel());
    }

    /** 轮询兜底通道；pubsub-only 模式不轮询 */
    @Scheduled(fixedDelayString = "${dag.scheduler.interval-ms:5000}")
    public void tick() {
        if (DagConstants.CHANNEL_PUBSUB.equalsIgnoreCase(properties.getChannel())) {
            return;
        }
        scan();
    }

    /** 事件唤醒通道（DagWakeupListener 调用）；db-only 模式忽略事件 */
    public void wakeUp() {
        log.debug("micro-dag: 收到调度唤醒");
        if (DagConstants.CHANNEL_DB.equalsIgnoreCase(properties.getChannel())) {
            return;
        }
        scan();
    }

    synchronized void scan() {
        log.debug("micro-dag: 调度扫描开始");
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
        log.debug("micro-dag: 调度扫描结束 runningInstances={}", runningInstances.size());
    }

    private void reclaimExpired(LocalDateTime now) {
        List<DagNodeInstance> expired = nodeMapper.listExpiredLease(now);
        if (expired.isEmpty()) {
            return;
        }
        int count = nodeMapper.reclaimExpiredLease(now);
        for (DagNodeInstance node : expired) {
            // 与 reclaimExpiredLease 的 CASE 同规则：attempt > max_retry 视为重试耗尽，节点已置 FAILED
            boolean exhausted = (node.getAttempt() == null ? 0 : node.getAttempt())
                > (node.getMaxRetry() == null ? 0 : node.getMaxRetry());
            if (exhausted) {
                logService.log(node.getInstanceCode(), node.getNodeCode(), node.getAttempt(),
                    ExecutionEvent.FAIL, JsonUtils.toJson(Map.of("error", "租约过期回收，重试次数已耗尽")), node.getLeaseOwner());
            } else {
                logService.log(node.getInstanceCode(), node.getNodeCode(), node.getAttempt(),
                    ExecutionEvent.LEASE_RECLAIM, null, node.getLeaseOwner());
            }
        }
        log.info("micro-dag: 租约回收 {} 个节点", count);
    }

    private void submitReadyNodes(DagInstance instance, LocalDateTime now) {
        List<DagNodeInstance> nodes = nodeMapper.listByInstanceCode(instance.getInstanceCode());
        DagDefModel def = versionService.load(instance.getDagCode(), instance.getDagVersion());
        Map<String, NodeStatus> statusMap = new HashMap<>();
        Map<String, Map<String, Object>> outputs = new HashMap<>();
        for (DagNodeInstance n : nodes) {
            statusMap.put(n.getNodeCode(), NodeStatus.valueOf(n.getStatus()));
            outputs.put(n.getNodeCode(), JsonUtils.parseMap(n.getOutputJson()));
        }
        Map<String, Object> ctx = JsonUtils.parseMap(instance.getContextJson());

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
            LocalDateTime expire = now.plusNanos(DagLeaseHelper.leaseDuration(properties, node) * DagConstants.MS_TO_NS);
            NodeInstanceLeaseDto lease = new NodeInstanceLeaseDto();
            lease.setId(node.getId());
            lease.setVersion(node.getVersion());
            lease.setLeaseOwner(workerId);
            lease.setLeaseExpireTime(expire);
            int ok = nodeMapper.casClaim(lease);
            if (ok == 1) {
                claimed++;
                node.setStatus(NodeStatus.RUNNING.name());
                node.setLeaseOwner(workerId);
                node.setLeaseExpireTime(expire);
                node.setAttempt(node.getAttempt() == null ? 1 : node.getAttempt() + 1);
                logService.log(node.getInstanceCode(), node.getNodeCode(), node.getAttempt(),
                    ExecutionEvent.CLAIM, null, workerId);
                if (isPassthroughNode(def, node.getNodeCode())) {
                    // 存量兼容：START/END/BRANCH 且未配置 handler 的节点，领取后直达成功，由引擎级联推进下游；
                    // 配置了 handler 的节点（含 system-* 与业务 handler）一律走 worker 执行链路
                    engineService.onSuccess(instance.getInstanceCode(), node.getNodeCode(), Map.of());
                } else {
                    worker.submit(node, instance);
                }
            }
        }
        if (claimed > 0) {
            log.info("micro-dag: 本轮领取 instanceCode={} claimed={}", instance.getInstanceCode(), claimed);
        }
    }

    /** 判断定义中该节点是否为直达成功节点（START/END/BRANCH 且 handler 为空的存量兼容）；定义缺失该节点时按 NORMAL 走 worker */
    private boolean isPassthroughNode(DagDefModel def, String nodeCode) {
        if (def == null || def.getNodes() == null) {
            return false;
        }
        for (NodeDef n : def.getNodes()) {
            if (n.getCode().equals(nodeCode)) {
                return NodeType.specialOf(n.getNodeType()) != null && StringUtils.isBlank(n.getHandler());
            }
        }
        return false;
    }
}
