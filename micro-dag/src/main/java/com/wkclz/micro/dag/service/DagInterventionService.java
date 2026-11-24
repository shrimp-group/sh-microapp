package com.wkclz.micro.dag.service;

import com.wkclz.core.exception.ValidationException;
import com.wkclz.micro.dag.bean.dto.DagDefModel;
import com.wkclz.micro.dag.bean.dto.EdgeDef;
import com.wkclz.micro.dag.bean.entity.DagInstance;
import com.wkclz.micro.dag.bean.entity.DagNodeInstance;
import com.wkclz.micro.dag.bean.enums.ExecutionEvent;
import com.wkclz.micro.dag.bean.enums.InstanceStatus;
import com.wkclz.micro.dag.bean.enums.NodeStatus;
import com.wkclz.micro.dag.bean.req.InstanceRerunFromReq;
import com.wkclz.micro.dag.bean.req.NodeInterveneReq;
import com.wkclz.micro.dag.cache.DagRedisPublisher;
import com.wkclz.micro.dag.helper.DagKeys;
import com.wkclz.micro.dag.mapper.DagNodeInstanceMapper;
import com.wkclz.micro.dag.utils.JsonUtils;
import com.wkclz.mybatis.service.BaseService;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 人工干预治理：中断/改参/跳过/重置/重跑/从指定节点重跑/终止（操作人由 MyBatisUpdateInterceptor 写入 create_by/update_by）
 */
@Slf4j
@Service
public class DagInterventionService extends BaseService<DagNodeInstance, DagNodeInstanceMapper> {

    @Resource
    private DagInstanceService instanceService;
    @Resource
    private DagProcessVersionService versionService;
    @Resource
    private DagEngineService engine;
    @Resource
    private DagExecutionLogService logService;
    @Resource
    private DagRedisPublisher publisher;

    @PostConstruct
    public void init() {
        log.info("micro-dag: 初始化人工干预服务");
    }

    /** interrupt：RUNNING 校验 → interrupt_flag=1 → Redis 广播（本机 cancel + 异机忽略） */
    @Transactional
    public void interrupt(String instanceCode, String nodeCode) {
        log.info("micro-dag: 人工中断 instanceCode={} nodeCode={}", instanceCode, nodeCode);
        DagNodeInstance node = mustExist(instanceCode, nodeCode);
        if (!NodeStatus.RUNNING.name().equals(node.getStatus())) {
            throw ValidationException.of("仅 RUNNING 节点可中断，当前状态: " + node.getStatus());
        }
        node.setInterruptFlag(1);
        mapper.updateById(node);
        publisher.publishInterrupt(instanceCode, nodeCode);
    }

    /** params：非运行态改节点参数，重跑时生效 */
    @Transactional
    public void params(NodeInterveneReq req) {
        log.info("micro-dag: 人工改参 instanceCode={} nodeCode={}", req.getInstanceCode(), req.getNodeCode());
        DagNodeInstance node = mustExist(req.getInstanceCode(), req.getNodeCode());
        if (NodeStatus.RUNNING.name().equals(node.getStatus())) {
            throw ValidationException.of("RUNNING 节点不可改参，请先中断/终止");
        }
        node.setInputParamsJson(req.getInputParamsJson());
        mapper.updateById(node);
    }

    /**
     * skip：非运行态置 SKIPPED + skip_reason，触发下游就绪计算。
     * 跳过补救：下游闭包内被级联跳过的节点重置 PENDING 重新调度（跳过视为上游满足，下游继续执行），
     * 实例 FAILED 时回 RUNNING；人工跳过的下游节点不受影响
     */
    @Transactional
    public void skip(NodeInterveneReq req) {
        log.info("micro-dag: 人工跳过 instanceCode={} nodeCode={} reason={}", req.getInstanceCode(), req.getNodeCode(), req.getSkipReason());
        DagNodeInstance node = mustExist(req.getInstanceCode(), req.getNodeCode());
        if (NodeStatus.RUNNING.name().equals(node.getStatus())) {
            throw ValidationException.of("RUNNING 节点不可跳过，请先中断/终止");
        }
        node.setStatus(NodeStatus.SKIPPED.name());
        node.setSkipReason(StringUtils.defaultIfBlank(req.getSkipReason(), "人工跳过"));
        mapper.updateById(node);
        logService.log(node.getInstanceCode(), node.getNodeCode(), node.getAttempt(), ExecutionEvent.SKIP, null, null);
        rescueDownstream(node.getInstanceCode(), node.getNodeCode());
        engine.advance(node.getInstanceCode());
    }

    /** reset：置 PENDING，清除输出/异常/重试时间；clearParams=true 时同时清除节点参数。不自动触发调度 */
    @Transactional
    public void reset(NodeInterveneReq req) {
        log.info("micro-dag: 人工重置 instanceCode={} nodeCode={} clearParams={}", req.getInstanceCode(), req.getNodeCode(), req.getClearParams());
        DagNodeInstance node = mustExist(req.getInstanceCode(), req.getNodeCode());
        if (NodeStatus.RUNNING.name().equals(node.getStatus())) {
            throw ValidationException.of("RUNNING 节点不可重置，请先中断/终止");
        }
        doReset(node, Boolean.TRUE.equals(req.getClearParams()));
        logService.log(node.getInstanceCode(), node.getNodeCode(), node.getAttempt(), ExecutionEvent.RESET, null, null);
    }

    /** rerun：可选重设节点参数（非空校验合法后更新）+ 单节点 reset + 清理钩子判定 + 实例回 RUNNING + 唤醒调度 */
    @Transactional
    public void rerun(NodeInterveneReq req) {
        log.info("micro-dag: 人工重跑 instanceCode={} nodeCode={} hasParams={}",
            req.getInstanceCode(), req.getNodeCode(), StringUtils.isNotBlank(req.getInputParamsJson()));
        mustBeValidJson(req.getInputParamsJson(), "节点参数");
        DagNodeInstance node = mustExist(req.getInstanceCode(), req.getNodeCode());
        if (NodeStatus.RUNNING.name().equals(node.getStatus())) {
            throw ValidationException.of("RUNNING 节点不可重跑，请先中断/终止");
        }
        if (StringUtils.isNotBlank(req.getInputParamsJson())) {
            node.setInputParamsJson(req.getInputParamsJson());
        }
        node.setInterruptFlag(0);
        node.setCleanupFlag(hasHistory(node) ? 1 : 0);
        doReset(node, false);
        logService.log(node.getInstanceCode(), node.getNodeCode(), node.getAttempt(), ExecutionEvent.RESET, null, null);
        resumeInstance(node.getInstanceCode());
    }

    /** rerun-from：可选重设实例参数（同步 bizParamsJson 与 contextJson）+ 目标节点及全部下游 reset（上游 SUCCESS 保留），实例回 RUNNING */
    @Transactional
    public void rerunFrom(InstanceRerunFromReq req) {
        String instanceCode = req.getInstanceCode();
        log.info("micro-dag: 从指定节点重跑 instanceCode={} nodeCode={} hasParams={}",
            instanceCode, req.getNodeCode(), StringUtils.isNotBlank(req.getInstanceParamsJson()));
        mustBeValidJson(req.getInstanceParamsJson(), "实例参数");
        DagInstance instance = instanceService.selectOneByEntity(instanceQuery(instanceCode));
        if (instance == null) {
            throw ValidationException.of("实例不存在: " + instanceCode);
        }
        if (InstanceStatus.RUNNING.name().equals(instance.getStatus())) {
            throw ValidationException.of("实例运行中不可 rerun-from，请先终止");
        }
        if (StringUtils.isNotBlank(req.getInstanceParamsJson())) {
            instance.setBizParamsJson(req.getInstanceParamsJson());
            instance.setContextJson(req.getInstanceParamsJson());
        }
        DagDefModel def = versionService.load(instance.getDagCode(), instance.getDagVersion());
        Set<String> downstream = downstreamClosure(def, req.getNodeCode());
        List<DagNodeInstance> nodes = mapper.listByInstanceCode(instanceCode);
        for (DagNodeInstance n : nodes) {
            if (!downstream.contains(n.getNodeCode())) {
                continue;
            }
            n.setInterruptFlag(0);
            n.setCleanupFlag(hasHistory(n) ? 1 : 0);
            doReset(n, false);
            logService.log(n.getInstanceCode(), n.getNodeCode(), n.getAttempt(), ExecutionEvent.RESET, null, null);
        }
        instance.setStatus(InstanceStatus.RUNNING.name());
        instanceService.updateById(instance);
        publisher.wakeUp();
    }

    /** terminate：实例置 TERMINATED；RUNNING 实例的运行中节点走 interrupt 流程 */
    @Transactional
    public void terminate(String instanceCode) {
        log.info("micro-dag: 终止实例 instanceCode={}", instanceCode);
        DagInstance instance = instanceService.selectOneByEntity(instanceQuery(instanceCode));
        if (instance == null) {
            throw ValidationException.of("实例不存在: " + instanceCode);
        }
        String status = instance.getStatus();
        if (!InstanceStatus.RUNNING.name().equals(status) && !InstanceStatus.FAILED.name().equals(status)) {
            throw ValidationException.of("仅 RUNNING/FAILED 实例可终止，当前状态: " + status);
        }
        boolean wasRunning = InstanceStatus.RUNNING.name().equals(status);
        instance.setStatus(InstanceStatus.TERMINATED.name());
        instanceService.updateById(instance);
        if (wasRunning) {
            for (DagNodeInstance n : mapper.listByInstanceCode(instanceCode)) {
                if (NodeStatus.RUNNING.name().equals(n.getStatus())) {
                    n.setInterruptFlag(1);
                    mapper.updateById(n);
                    publisher.publishInterrupt(n.getInstanceCode(), n.getNodeCode());
                }
            }
        }
    }

    // ---------- 内部方法 ----------

    /** 按 instance_code + node_code（唯一键）定位节点实例 */
    private DagNodeInstance mustExist(String instanceCode, String nodeCode) {
        DagNodeInstance where = new DagNodeInstance();
        where.setInstanceCode(instanceCode);
        where.setNodeCode(nodeCode);
        DagNodeInstance node = selectOneByEntity(where);
        if (node == null) {
            throw ValidationException.of("节点实例不存在: " + DagKeys.instanceNodeKey(instanceCode, nodeCode));
        }
        return node;
    }

    private DagInstance instanceQuery(String instanceCode) {
        DagInstance where = new DagInstance();
        where.setInstanceCode(instanceCode);
        return where;
    }

    /** 非空 JSON 校验：空值放行，非法抛 ValidationException（阻止带脏参数重跑） */
    private void mustBeValidJson(String json, String label) {
        if (StringUtils.isBlank(json)) {
            return;
        }
        try {
            JsonUtils.readValue(json, Map.class);
        } catch (Exception e) {
            log.warn("micro-dag: {} JSON 非法 json={}", label, json, e);
            throw ValidationException.of(label + " JSON 非法");
        }
    }

    private void doReset(DagNodeInstance node, boolean clearParams) {
        node.setStatus(NodeStatus.PENDING.name());
        node.setErrorMsg(null);
        node.setOutputJson(null);
        node.setNextRetryTime(null);
        node.setSkipReason(null);
        node.setStopReason(null);
        if (clearParams) {
            node.setInputParamsJson(null);
        }
        mapper.updateById(node);
    }

    /** 历史执行判定：attempt > 0 即发生过领取执行 → 重跑前需清理钩子 */
    private boolean hasHistory(DagNodeInstance node) {
        return node.getAttempt() != null && node.getAttempt() > 0;
    }

    /**
     * 跳过补救 + 实例恢复：下游闭包内凭级联跳过原因识别被连锁 SKIPPED 的节点，重置 PENDING 重新调度；
     * 实例 FAILED 时一律回 RUNNING（skip 表达继续意图，下游 PENDING 等待节点由调度器继续推进；终止实例不恢复）
     */
    private void rescueDownstream(String instanceCode, String nodeCode) {
        DagInstance instance = instanceService.selectOneByEntity(instanceQuery(instanceCode));
        if (instance == null) {
            return;
        }
        DagDefModel def = versionService.load(instance.getDagCode(), instance.getDagVersion());
        Set<String> downstream = downstreamClosure(def, nodeCode);
        int rescued = 0;
        for (DagNodeInstance n : mapper.listByInstanceCode(instanceCode)) {
            if (downstream.contains(n.getNodeCode()) && NodeStatus.SKIPPED.name().equals(n.getStatus())
                && DagEngineService.CASCADE_SKIP_REASON.equals(n.getSkipReason())) {
                doReset(n, false);
                logService.log(n.getInstanceCode(), n.getNodeCode(), n.getAttempt(), ExecutionEvent.RESET, null, null);
                rescued++;
            }
        }
        if (rescued > 0) {
            log.info("micro-dag: 跳过补救恢复下游节点数={} instanceCode={}", rescued, instanceCode);
        }
        if (InstanceStatus.FAILED.name().equals(instance.getStatus())) {
            log.info("micro-dag: 跳过后恢复实例 instanceCode={}", instanceCode);
            instance.setStatus(InstanceStatus.RUNNING.name());
            instanceService.updateById(instance);
        }
    }

    private void resumeInstance(String instanceCode) {
        DagInstance instance = instanceService.selectOneByEntity(instanceQuery(instanceCode));
        if (instance == null) {
            throw ValidationException.of("实例不存在: " + instanceCode);
        }
        instance.setStatus(InstanceStatus.RUNNING.name());
        instanceService.updateById(instance);
        publisher.wakeUp();
    }

    /** 目标节点 + 全部下游闭包（BFS 沿出边） */
    private Set<String> downstreamClosure(DagDefModel def, String nodeCode) {
        Set<String> closure = new HashSet<>();
        Deque<String> queue = new ArrayDeque<>();
        closure.add(nodeCode);
        queue.add(nodeCode);
        while (!queue.isEmpty()) {
            String from = queue.poll();
            for (EdgeDef e : def.getEdges() == null ? List.<EdgeDef>of() : def.getEdges()) {
                if (from.equals(e.getFromCode()) && closure.add(e.getToCode())) {
                    queue.add(e.getToCode());
                }
            }
        }
        return closure;
    }
}
