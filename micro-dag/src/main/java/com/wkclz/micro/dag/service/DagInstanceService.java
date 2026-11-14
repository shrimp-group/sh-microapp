package com.wkclz.micro.dag.service;

import com.wkclz.core.base.PageData;
import com.wkclz.core.exception.ValidationException;
import com.wkclz.micro.dag.bean.dto.DagDefModel;
import com.wkclz.micro.dag.bean.dto.NodeDef;
import com.wkclz.micro.dag.bean.entity.DagDefinition;
import com.wkclz.micro.dag.bean.entity.DagInstance;
import com.wkclz.micro.dag.bean.entity.DagNodeInstance;
import com.wkclz.micro.dag.bean.enums.BackoffType;
import com.wkclz.micro.dag.bean.enums.DefinitionStatus;
import com.wkclz.micro.dag.bean.enums.InstanceStatus;
import com.wkclz.micro.dag.bean.enums.NodeStatus;
import com.wkclz.micro.dag.bean.req.InstancePageReq;
import com.wkclz.micro.dag.bean.req.LaunchReq;
import com.wkclz.micro.dag.bean.resp.InstanceDagViewResp;
import com.wkclz.micro.dag.bean.resp.InstanceResp;
import com.wkclz.micro.dag.bean.resp.NodeInstanceResp;
import com.wkclz.micro.dag.mapper.DagInstanceMapper;
import com.wkclz.micro.dag.mapper.DagNodeInstanceMapper;
import com.wkclz.mybatis.service.BaseService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

@Slf4j
@Service
public class DagInstanceService extends BaseService<DagInstance, DagInstanceMapper> {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private final DagDefinitionService definitionService;
    private final DagNodeInstanceMapper nodeInstanceMapper;

    public DagInstanceService(DagDefinitionService definitionService, DagNodeInstanceMapper nodeInstanceMapper) {
        this.definitionService = definitionService;
        this.nodeInstanceMapper = nodeInstanceMapper;
    }

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

        DagDefinition query = new DagDefinition();
        query.setDagCode(req.getDagCode());
        DagDefinition def = definitionService.selectByEntity(query).stream()
            .filter(d -> DefinitionStatus.PUBLISHED.name().equals(d.getStatus()))
            .max(Comparator.comparingInt(DagDefinition::getDagVersion))
            .orElseThrow(() -> ValidationException.of("无可用的 PUBLISHED 定义: " + req.getDagCode()));
        DagDefModel dagDef = definitionService.load(def.getDagCode(), def.getDagVersion());
        if (dagDef.getNodes() == null || dagDef.getNodes().isEmpty()) {
            throw ValidationException.of("定义无节点: " + req.getDagCode());
        }

        String paramsJson = toJson(req.getParams() == null ? Map.of() : req.getParams());
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
            Map<String, Object> merged = new HashMap<>();
            if (nd.getParams() != null) {
                merged.putAll(nd.getParams());
            }
            if (req.getParams() != null) {
                merged.putAll(req.getParams());
            }
            n.setInputParamsJson(toJson(merged));
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
        log.info("micro-dag: 实例分页 instanceCode={} traceCode={} status={}", req.getInstanceCode(), req.getTraceCode(), req.getStatus());
        DagInstance query = new DagInstance();
        query.setInstanceCode(req.getInstanceCode());
        query.setTraceCode(req.getTraceCode());
        query.setStatus(req.getStatus());
        query.setDagCode(req.getDagCode());
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
        DagDefModel dagDef = definitionService.load(instance.getDagCode(), instance.getDagVersion());
        Map<String, NodeDef> defMap = dagDef.getNodes() == null ? Map.of()
            : dagDef.getNodes().stream().collect(Collectors.toMap(NodeDef::getCode, n -> n));
        List<NodeInstanceResp> nodeResps = nodes.stream().map(n -> {
            NodeInstanceResp r = new NodeInstanceResp();
            BeanUtils.copyProperties(n, r);
            NodeDef nd = defMap.get(n.getNodeCode());
            if (nd != null) {
                r.setPosX(nd.getPosX());
                r.setPosY(nd.getPosY());
            }
            return r;
        }).toList();
        view.setNodes(nodeResps);

        Map<String, Map<String, Object>> outputs = new HashMap<>();
        for (DagNodeInstance n : nodes) {
            outputs.put(n.getNodeCode(), parseMap(n.getOutputJson()));
        }
        view.setNodeOutputs(outputs);
        view.setCtx(parseMap(instance.getContextJson()));

        // 画布连线与节点坐标均来自定义快照（posX/posY 已随 NodeInstanceResp 带出）
        view.setEdges(dagDef.getEdges());
        return view;
    }

    private InstanceResp toResp(DagInstance instance) {
        InstanceResp resp = new InstanceResp();
        BeanUtils.copyProperties(instance, resp);
        return resp;
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

    /** instance_code 生成（模块内实现：时间戳+随机，不依赖 micro-seq） */
    private String newInstanceCode() {
        return "INST" + System.currentTimeMillis() + ThreadLocalRandom.current().nextInt(1000, 9999);
    }

    private String toJson(Object obj) {
        try {
            return OBJECT_MAPPER.writeValueAsString(obj);
        } catch (Exception e) {
            log.error("micro-dag: 参数序列化失败", e);
            throw ValidationException.of("参数序列化失败: " + e.getMessage());
        }
    }
}
