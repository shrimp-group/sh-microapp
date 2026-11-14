package com.wkclz.micro.dag.service;

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
import com.wkclz.micro.dag.bean.req.LaunchReq;
import com.wkclz.micro.dag.mapper.DagInstanceMapper;
import com.wkclz.micro.dag.mapper.DagNodeInstanceMapper;
import com.wkclz.mybatis.service.BaseService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

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
