package com.wkclz.micro.dag.service;

import com.wkclz.core.exception.ValidationException;
import com.wkclz.micro.dag.bean.dto.DagDefModel;
import com.wkclz.micro.dag.bean.entity.DagInstance;
import com.wkclz.micro.dag.bean.entity.DagNodeInstance;
import com.wkclz.micro.dag.bean.enums.BackoffType;
import com.wkclz.micro.dag.bean.enums.ExecutionEvent;
import com.wkclz.micro.dag.bean.enums.InstanceStatus;
import com.wkclz.micro.dag.bean.enums.NodeStatus;
import com.wkclz.micro.dag.cache.DagRedisPublisher;
import com.wkclz.micro.dag.core.DagReadyCalculator;
import com.wkclz.micro.dag.core.DagRetryCalculator;
import com.wkclz.micro.dag.mapper.DagNodeInstanceMapper;
import com.wkclz.mybatis.service.BaseService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * 推进引擎：节点成功/失败/中断落库 → 级联跳过 → 实例终态 → Redis 唤醒调度（双通道的"事件即时"侧）
 */
@Slf4j
@Service
public class DagEngineService extends BaseService<DagNodeInstance, DagNodeInstanceMapper> {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final int MAX_ERROR_LEN = 1000;

    private final DagInstanceService instanceService;
    private final DagDefinitionService definitionService;
    private final DagExecutionLogService logService;
    private final DagReadyCalculator readyCalculator;
    private final DagRetryCalculator retryCalculator;
    private final DagRedisPublisher publisher;

    public DagEngineService(DagInstanceService instanceService, DagDefinitionService definitionService,
                            DagExecutionLogService logService, DagReadyCalculator readyCalculator,
                            DagRetryCalculator retryCalculator, DagRedisPublisher publisher) {
        this.instanceService = instanceService;
        this.definitionService = definitionService;
        this.logService = logService;
        this.readyCalculator = readyCalculator;
        this.retryCalculator = retryCalculator;
        this.publisher = publisher;
    }

    /** 节点成功回调（worker 线程调用）。防重投递：仅 RUNNING 状态落库 */
    @Transactional
    public void onSuccess(String instanceCode, String nodeCode, Map<String, Object> output) {
        log.info("micro-dag: 节点成功回调 instanceCode={} nodeCode={}", instanceCode, nodeCode);
        DagNodeInstance node = mustRunning(instanceCode, nodeCode);
        if (node == null) {
            return; // 防重投递：非 RUNNING 的重复回调直接忽略
        }
        node.setStatus(NodeStatus.SUCCESS.name());
        node.setOutputJson(toJson(output == null ? Map.of() : output));
        node.setCleanupFlag(0);
        node.setErrorMsg(null);
        node.setNextRetryTime(null);
        updateNode(node);
        logService.log(node.getInstanceCode(), node.getNodeCode(), node.getAttempt(),
            ExecutionEvent.SUCCESS, null, node.getLeaseOwner());
        advance(node.getInstanceCode());
    }

    /** 节点失败回调：attempt ≤ maxRetry → RETRYING，否则 FAILED */
    @Transactional
    public void onFailure(String instanceCode, String nodeCode, ExecutionEvent failEvent, String errorMsg) {
        log.info("micro-dag: 节点失败回调 instanceCode={} nodeCode={} event={}", instanceCode, nodeCode, failEvent);
        DagNodeInstance node = mustRunning(instanceCode, nodeCode);
        if (node == null) {
            return;
        }
        int attempt = node.getAttempt() == null ? 1 : node.getAttempt();
        String err = truncate(errorMsg);
        if (attempt <= (node.getMaxRetry() == null ? 0 : node.getMaxRetry())) {
            LocalDateTime next = retryCalculator.nextRetryTime(
                BackoffType.valueOf(node.getBackoff()), attempt,
                node.getIntervalSec() == null ? 0 : node.getIntervalSec(), LocalDateTime.now());
            node.setStatus(NodeStatus.RETRYING.name());
            node.setNextRetryTime(next);
            node.setErrorMsg(err);
            updateNode(node);
            logService.log(node.getInstanceCode(), node.getNodeCode(), attempt,
                ExecutionEvent.RETRY_SCHEDULED, toJson(Map.of("nextRetryTime", next.toString(), "error", err == null ? "" : err)),
                node.getLeaseOwner());
        } else {
            node.setStatus(NodeStatus.FAILED.name());
            node.setErrorMsg(err);
            node.setNextRetryTime(null);
            updateNode(node);
            logService.log(node.getInstanceCode(), node.getNodeCode(), attempt, failEvent,
                toJson(Map.of("error", err == null ? "" : err)), node.getLeaseOwner());
        }
        advance(node.getInstanceCode());
    }

    /** 中断回调：interrupt_flag=1 或协作式中断 → MANUAL_STOPPED */
    @Transactional
    public void onInterrupted(String instanceCode, String nodeCode) {
        log.info("micro-dag: 节点中断回调 instanceCode={} nodeCode={}", instanceCode, nodeCode);
        DagNodeInstance node = mustRunning(instanceCode, nodeCode);
        if (node == null) {
            return;
        }
        node.setStatus(NodeStatus.MANUAL_STOPPED.name());
        node.setStopReason("人工中断");
        updateNode(node);
        logService.log(node.getInstanceCode(), node.getNodeCode(), node.getAttempt(),
            ExecutionEvent.INTERRUPT, null, node.getLeaseOwner());
        advance(node.getInstanceCode());
    }

    /**
     * 推进实例：级联跳过 → 实例终态 → Redis 唤醒调度器（就绪领取统一走调度器，保持单一 CAS 入口）。
     * 人工干预（skip / rerun 等）复用本方法触发下游就绪计算。
     */
    public void advance(String instanceCode) {
        DagInstance where = new DagInstance();
        where.setInstanceCode(instanceCode);
        DagInstance instance = instanceService.selectOneByEntity(where);
        if (instance == null || !InstanceStatus.RUNNING.name().equals(instance.getStatus())) {
            log.info("micro-dag: 实例非 RUNNING，跳过推进 instanceCode={}", instanceCode);
            return;
        }
        List<DagNodeInstance> nodes = mapper.listByInstanceCode(instanceCode);
        DagDefModel def = loadDef(instance);
        Map<String, NodeStatus> statusMap = new HashMap<>();
        Map<String, Map<String, Object>> outputs = new HashMap<>();
        for (DagNodeInstance n : nodes) {
            statusMap.put(n.getNodeCode(), NodeStatus.valueOf(n.getStatus()));
            outputs.put(n.getNodeCode(), parseMap(n.getOutputJson()));
        }
        Map<String, Object> ctx = parseMap(instance.getContextJson());

        Set<String> skips = readyCalculator.cascadeSkips(def, statusMap, ctx, outputs);
        for (DagNodeInstance n : nodes) {
            if (skips.contains(n.getNodeCode())) {
                n.setStatus(NodeStatus.SKIPPED.name());
                n.setSkipReason("上游分支条件不满足");
                updateNode(n);
                logService.log(n.getInstanceCode(), n.getNodeCode(), n.getAttempt(), ExecutionEvent.SKIP, null, null);
                statusMap.put(n.getNodeCode(), NodeStatus.SKIPPED);
            }
        }

        Optional<String> terminal = readyCalculator.instanceTerminalStatus(statusMap);
        if (terminal.isPresent()) {
            instance.setStatus(terminal.get());
            instanceService.updateById(instance);
            log.info("micro-dag: 实例到达终态 instanceCode={} status={}", instanceCode, terminal.get());
        }

        publisher.wakeUp();
    }

    /** 按 instance_code + node_code 定位节点实例，非 RUNNING 返回 null（防重投递） */
    private DagNodeInstance mustRunning(String instanceCode, String nodeCode) {
        DagNodeInstance where = new DagNodeInstance();
        where.setInstanceCode(instanceCode);
        where.setNodeCode(nodeCode);
        DagNodeInstance node = mapper.selectOneByEntity(where);
        if (node == null) {
            throw ValidationException.of("节点实例不存在: " + instanceCode + ":" + nodeCode);
        }
        if (!NodeStatus.RUNNING.name().equals(node.getStatus())) {
            log.info("micro-dag: 节点非 RUNNING，忽略重复回调 instanceCode={} nodeCode={} status={}", instanceCode, nodeCode, node.getStatus());
            return null;
        }
        return node;
    }

    private void updateNode(DagNodeInstance node) {
        if (mapper.updateById(node) != 1) {
            log.error("micro-dag: 节点状态落库失败(乐观锁冲突或已被回收) id={} node={}", node.getId(), node.getNodeCode());
        }
    }

    /** 加载定义（dagCode + dagVersion 唯一定位，由 dag_node/dag_edge 组装内存模型） */
    private DagDefModel loadDef(DagInstance instance) {
        return definitionService.load(instance.getDagCode(), instance.getDagVersion());
    }

    private String truncate(String msg) {
        if (msg == null || msg.length() <= MAX_ERROR_LEN) {
            return msg;
        }
        return msg.substring(0, MAX_ERROR_LEN);
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

    private String toJson(Object obj) {
        try {
            return OBJECT_MAPPER.writeValueAsString(obj);
        } catch (Exception e) {
            log.error("micro-dag: JSON 序列化失败", e);
            return "{}";
        }
    }
}
