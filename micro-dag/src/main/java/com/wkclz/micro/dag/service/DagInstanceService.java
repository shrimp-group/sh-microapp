package com.wkclz.micro.dag.service;

import com.wkclz.core.base.PageData;
import com.wkclz.core.exception.ValidationException;
import com.wkclz.micro.dag.bean.constant.DagConstants;
import com.wkclz.micro.dag.bean.dto.DagDefModel;
import com.wkclz.micro.dag.bean.dto.EdgeDef;
import com.wkclz.micro.dag.bean.dto.NodeDef;
import com.wkclz.micro.dag.bean.entity.DagProcessVersion;
import com.wkclz.micro.dag.bean.entity.DagExecutionLog;
import com.wkclz.micro.dag.bean.entity.DagInstance;
import com.wkclz.micro.dag.bean.entity.DagNodeInstance;
import com.wkclz.micro.dag.bean.enums.BackoffType;
import com.wkclz.micro.dag.bean.enums.ExecutionEvent;
import com.wkclz.micro.dag.bean.enums.ProcessStatus;
import com.wkclz.micro.dag.bean.enums.InstanceStatus;
import com.wkclz.micro.dag.bean.enums.NodeStatus;
import com.wkclz.micro.dag.bean.enums.NodeType;
import com.wkclz.micro.dag.bean.req.InstancePageReq;
import com.wkclz.micro.dag.bean.req.LaunchReq;
import com.wkclz.micro.dag.bean.resp.InstanceDagViewResp;
import com.wkclz.micro.dag.bean.resp.InstanceResp;
import com.wkclz.micro.dag.bean.resp.NodeInstanceResp;
import com.wkclz.micro.dag.helper.DagVersionHelper;
import com.wkclz.micro.dag.mapper.DagInstanceMapper;
import com.wkclz.micro.dag.mapper.DagNodeInstanceMapper;
import com.wkclz.micro.dag.utils.JsonUtils;
import com.wkclz.mybatis.service.BaseService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

@Slf4j
@Service
public class DagInstanceService extends BaseService<DagInstance, DagInstanceMapper> {

    /** 一次执行的终态事件：耗时自 CLAIM 起算至该集合中的事件（LEASE_RECLAIM 为租约过期时该次的兜底终态） */
    private static final Set<String> COST_TERMINAL_EVENTS = Set.of(
        ExecutionEvent.SUCCESS.name(), ExecutionEvent.FAIL.name(), ExecutionEvent.NOT_READY.name(),
        ExecutionEvent.TIMEOUT.name(), ExecutionEvent.INTERRUPT.name(), ExecutionEvent.LEASE_RECLAIM.name());

    @Resource
    private DagProcessVersionService versionService;
    @Resource
    private DagNodeInstanceMapper nodeInstanceMapper;
    @Resource
    private DagExecutionLogService executionLogService;

    /**
     * 发起实例：锁定 PUBLISHED 定义 → 生成 instance_code → 创建实例 + 全部节点实例（重试/超时/参数快照）
     */
    @Transactional
    public Long launch(LaunchReq req) {
        log.info("micro-dag: 发起实例 dagCode={} traceCode={}", req.getDagCode(), req.getTraceCode());
        if (StringUtils.isBlank(req.getDagCode())) {
            throw ValidationException.of("dagCode 不能为空");
        }
        if (StringUtils.isNotBlank(req.getTraceCode())) {
            DagInstance probe = new DagInstance();
            probe.setTraceCode(req.getTraceCode());
            if (selectCountByEntity(probe) > 0) {
                throw ValidationException.of("traceCode 已存在: " + req.getTraceCode());
            }
        }

        DagProcessVersion query = new DagProcessVersion();
        query.setDagCode(req.getDagCode());
        DagProcessVersion def = DagVersionHelper.maxVersionOf(versionService.selectByEntity(query), ProcessStatus.PUBLISHED.name());
        if (def == null) {
            throw ValidationException.of("无可用的 PUBLISHED 定义: " + req.getDagCode());
        }
        DagDefModel dagDef = versionService.load(def.getDagCode(), def.getDagVersion());
        if (dagDef.getNodes() == null || dagDef.getNodes().isEmpty()) {
            throw ValidationException.of("定义无节点: " + req.getDagCode());
        }

        String paramsJson = JsonUtils.toJsonStrict(req.getParams() == null ? Map.of() : req.getParams());
        DagInstance instance = new DagInstance();
        instance.setInstanceCode(newInstanceCode());
        instance.setTraceCode(req.getTraceCode());
        instance.setDagCode(def.getDagCode());
        instance.setDagVersion(def.getDagVersion());
        instance.setStatus(InstanceStatus.RUNNING.name());
        instance.setContextJson(paramsJson);
        instance.setBizParamsJson(paramsJson);
        insert(instance);

        List<DagNodeInstance> nodes = new ArrayList<>();
        for (NodeDef nd : dagDef.getNodes()) {
            DagNodeInstance n = new DagNodeInstance();
            n.setInstanceCode(instance.getInstanceCode());
            n.setNodeCode(nd.getCode());
            n.setNodeName(nd.getName());
            n.setHandler(nd.getHandler());
            n.setStatus(NodeStatus.PENDING.name());
            n.setAttempt(0);
            n.setMaxRetry(nd.getMaxRetry() == null ? 0 : nd.getMaxRetry());
            n.setIntervalSec(nd.getIntervalSec() == null ? 0 : nd.getIntervalSec());
            n.setBackoff(nd.getBackoff() == null ? BackoffType.FIXED.name() : nd.getBackoff());
            n.setTimeoutMs(nd.getTimeoutMs());
            // BRANCH 节点：将出边条件（key=toCode，value=whenExpr）快照进输入参数，
            // 供 system-branch 求值与实例明细展示；branches 最后合并，实例参数不可覆盖分支系统键
            Map<String, Object> branches = new HashMap<>();
            if (NodeType.BRANCH.getCode().equals(nd.getNodeType()) && dagDef.getEdges() != null) {
                dagDef.getEdges().stream()
                    .filter(e -> nd.getCode().equals(e.getFromCode()) && DagConstants.EDGE_TYPE_CONDITION.equals(e.getType()))
                    .forEach(e -> branches.put(e.getToCode(), e.getWhenExpr()));
            }
            Map<String, Object> merged = new HashMap<>();
            if (nd.getParams() != null) {
                merged.putAll(nd.getParams());
            }
            if (req.getParams() != null) {
                merged.putAll(req.getParams());
            }
            if (!branches.isEmpty()) {
                merged.putAll(branches);
            }
            n.setInputParamsJson(JsonUtils.toJsonStrict(merged));
            n.setInterruptFlag(0);
            n.setCleanupFlag(0);
            nodes.add(n);
        }
        nodeInstanceMapper.insertBatch(nodes);
        log.info("micro-dag: 实例发起成功 id={} instanceCode={} dagCode={} nodes={}",
            instance.getId(), instance.getInstanceCode(), def.getDagCode(), nodes.size());
        return instance.getId();
    }

    /** 实例分页（instanceCode/traceCode/status/dagCode 等值过滤） */
    public PageData<InstanceResp> page(InstancePageReq req) {
        log.info("micro-dag: 实例分页 instanceCode={} traceCode={} status={} current={} size={}",
                req.getInstanceCode(), req.getTraceCode(), req.getStatus(), req.getCurrent(), req.getSize());
        DagInstance query = new DagInstance();
        query.setInstanceCode(req.getInstanceCode());
        query.setTraceCode(req.getTraceCode());
        query.setStatus(req.getStatus());
        query.setDagCode(req.getDagCode());
        // 分页参数在 Req 上承载，需复制到查询实体（selectPage 依赖实体的 current/size）
        query.setCurrent(req.getCurrent());
        query.setSize(req.getSize());
        PageData<DagInstance> page = selectPage(query);
        List<InstanceResp> list = page.getRecords().stream().map(this::toResp).toList();
        return PageData.convert(page, list);
    }

    /** 实例详情 + DAG 节点状态视图（设计器画布/监控共用数据结构） */
    public InstanceDagViewResp detail(String instanceCode) {
        log.info("micro-dag: 实例详情 instanceCode={}", instanceCode);
        DagInstance where = new DagInstance();
        where.setInstanceCode(instanceCode);
        DagInstance instance = selectOneByEntity(where);
        if (instance == null) {
            throw ValidationException.of("实例不存在: " + instanceCode);
        }
        InstanceDagViewResp view = new InstanceDagViewResp();
        view.setInstance(toResp(instance));

        List<DagNodeInstance> nodes = nodeInstanceMapper.listByInstanceCode(instance.getInstanceCode());
        DagDefModel dagDef = versionService.load(instance.getDagCode(), instance.getDagVersion());
        Map<String, NodeDef> defMap = dagDef.getNodes() == null ? Map.of()
            : dagDef.getNodes().stream().collect(Collectors.toMap(NodeDef::getCode, n -> n));
        // 最后一次执行耗时：供画布节点/详情气泡展示
        Map<String, Long> costMap = computeLastCostMs(nodes, executionLogService.listByInstance(instance.getInstanceCode()), LocalDateTime.now());
        List<NodeInstanceResp> nodeResps = new ArrayList<>();
        for (DagNodeInstance n : nodes) {
            nodeResps.add(toNodeResp(n, defMap, costMap));
        }
        view.setNodes(nodeResps);

        Map<String, Map<String, Object>> outputs = new HashMap<>();
        for (DagNodeInstance n : nodes) {
            outputs.put(n.getNodeCode(), JsonUtils.parseMap(n.getOutputJson()));
        }
        view.setNodeOutputs(outputs);
        view.setCtx(JsonUtils.parseMap(instance.getContextJson()));

        // 画布连线与节点坐标均来自定义快照（posX/posY 已随 NodeInstanceResp 带出）
        view.setEdges(dagDef.getEdges());
        return view;
    }

    /**
     * 各节点最后一次执行耗时（ms）：按节点最大 attempt 的 CLAIM 起算，至该 attempt 的终态事件；
     * 无终态事件且节点 RUNNING 时按当前时刻持续计算；从未领取的节点不计耗时
     */
    private Map<String, Long> computeLastCostMs(List<DagNodeInstance> nodes, List<DagExecutionLog> logs, LocalDateTime now) {
        // attempt → CLAIM 时间（终态事件结算后移除，剩余项即执行中未结的领取）
        Map<String, Map<Integer, LocalDateTime>> claims = new HashMap<>();
        Map<String, Long> costMap = new HashMap<>();
        for (DagExecutionLog l : logs) {
            if (l.getNodeCode() == null || l.getAttempt() == null || l.getCreateTime() == null) {
                continue;
            }
            Map<Integer, LocalDateTime> nodeClaims = claims.computeIfAbsent(l.getNodeCode(), k -> new HashMap<>());
            if (ExecutionEvent.CLAIM.name().equals(l.getEvent())) {
                nodeClaims.put(l.getAttempt(), l.getCreateTime());
            } else if (COST_TERMINAL_EVENTS.contains(l.getEvent())) {
                LocalDateTime claimTime = nodeClaims.remove(l.getAttempt());
                if (claimTime != null) {
                    costMap.put(l.getNodeCode(), Duration.between(claimTime, l.getCreateTime()).toMillis());
                }
            }
        }
        // RUNNING 中未结的领取：耗时随轮询刷新持续增长
        for (DagNodeInstance n : nodes) {
            if (!NodeStatus.RUNNING.name().equals(n.getStatus()) || n.getAttempt() == null) {
                continue;
            }
            Map<Integer, LocalDateTime> nodeClaims = claims.get(n.getNodeCode());
            LocalDateTime claimTime = nodeClaims == null ? null : nodeClaims.get(n.getAttempt());
            if (claimTime != null) {
                costMap.put(n.getNodeCode(), Duration.between(claimTime, now).toMillis());
            }
        }
        return costMap;
    }

    /** 节点实例响应组装：实例字段拷贝 + 定义坐标 + 最后一次执行耗时 */
    private NodeInstanceResp toNodeResp(DagNodeInstance n, Map<String, NodeDef> defMap, Map<String, Long> costMap) {
        NodeInstanceResp r = new NodeInstanceResp();
        BeanUtils.copyProperties(n, r);
        NodeDef nd = defMap.get(n.getNodeCode());
        if (nd != null) {
            r.setPosX(nd.getPosX());
            r.setPosY(nd.getPosY());
        }
        r.setLastCostMs(costMap.get(n.getNodeCode()));
        return r;
    }

    private InstanceResp toResp(DagInstance instance) {
        InstanceResp resp = new InstanceResp();
        BeanUtils.copyProperties(instance, resp);
        return resp;
    }

    /** instance_code 生成（模块内实现：时间戳+随机，不依赖 micro-seq） */
    private String newInstanceCode() {
        return "INST" + System.currentTimeMillis() + ThreadLocalRandom.current().nextInt(1000, 9999);
    }
}
