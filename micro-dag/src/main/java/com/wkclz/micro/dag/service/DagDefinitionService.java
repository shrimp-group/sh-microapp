package com.wkclz.micro.dag.service;

import com.wkclz.core.base.PageData;
import com.wkclz.core.exception.ValidationException;
import com.wkclz.micro.dag.api.NodeHandlerRegistry;
import com.wkclz.micro.dag.bean.dto.DagDefModel;
import com.wkclz.micro.dag.bean.dto.EdgeDef;
import com.wkclz.micro.dag.bean.dto.NodeDef;
import com.wkclz.micro.dag.bean.entity.DagDefinition;
import com.wkclz.micro.dag.bean.entity.DagEdge;
import com.wkclz.micro.dag.bean.entity.DagNode;
import com.wkclz.micro.dag.bean.enums.DefinitionStatus;
import com.wkclz.micro.dag.bean.req.DefinitionSaveReq;
import com.wkclz.micro.dag.bean.resp.DefinitionResp;
import com.wkclz.micro.dag.core.DagValidator;
import com.wkclz.micro.dag.mapper.DagDefinitionMapper;
import com.wkclz.micro.dag.mapper.DagEdgeMapper;
import com.wkclz.micro.dag.mapper.DagNodeMapper;
import com.wkclz.mybatis.service.BaseService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class DagDefinitionService extends BaseService<DagDefinition, DagDefinitionMapper> {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private final DagValidator validator;
    private final NodeHandlerRegistry registry;
    private final DagNodeMapper nodeMapper;
    private final DagEdgeMapper edgeMapper;

    public DagDefinitionService(DagValidator validator, NodeHandlerRegistry registry,
                                DagNodeMapper nodeMapper, DagEdgeMapper edgeMapper) {
        this.validator = validator;
        this.registry = registry;
        this.nodeMapper = nodeMapper;
        this.edgeMapper = edgeMapper;
    }

    /** 按版本查 dag_node/dag_edge 组装 DagDefModel（定义详情/引擎装配共用入口），定义不存在抛 ValidationException */
    public DagDefModel load(String dagCode, Integer dagVersion) {
        log.info("micro-dag: 加载定义 dagCode={} dagVersion={}", dagCode, dagVersion);
        DagDefinition defQuery = new DagDefinition();
        defQuery.setDagCode(dagCode);
        defQuery.setDagVersion(dagVersion);
        DagDefinition meta = selectOneByEntity(defQuery);
        if (meta == null) {
            throw ValidationException.of("定义不存在: " + dagCode + " v" + dagVersion);
        }
        DagNode nodeQuery = new DagNode();
        nodeQuery.setDagCode(dagCode);
        nodeQuery.setDagVersion(dagVersion);
        List<DagNode> nodeRows = nodeMapper.selectByEntity(nodeQuery);
        if (nodeRows == null || nodeRows.isEmpty()) {
            throw ValidationException.of("定义无节点: " + dagCode + " v" + dagVersion);
        }
        DagEdge edgeQuery = new DagEdge();
        edgeQuery.setDagCode(dagCode);
        edgeQuery.setDagVersion(dagVersion);
        List<DagEdge> edgeRows = edgeMapper.selectByEntity(edgeQuery);

        DagDefModel def = new DagDefModel();
        def.setDagCode(meta.getDagCode());
        def.setDagName(meta.getDagName());
        def.setDagVersion(meta.getDagVersion());
        def.setNodes(nodeRows.stream().map(this::toNodeDef).toList());
        def.setEdges(edgeRows == null ? List.of() : edgeRows.stream().map(this::toEdgeDef).toList());
        return def;
    }

    private NodeDef toNodeDef(DagNode row) {
        NodeDef n = new NodeDef();
        n.setCode(row.getNodeCode());
        n.setName(row.getNodeName());
        n.setHandler(row.getHandler());
        n.setTimeoutMs(row.getTimeoutMs());
        n.setMaxRetry(row.getMaxRetry());
        n.setIntervalSec(row.getIntervalSec());
        n.setBackoff(row.getBackoff());
        n.setParams(parseMap(row.getParamsJson()));
        n.setPosX(row.getPosX());
        n.setPosY(row.getPosY());
        return n;
    }

    private EdgeDef toEdgeDef(DagEdge row) {
        EdgeDef e = new EdgeDef();
        e.setEdgeCode(row.getEdgeCode());
        e.setFromCode(row.getFromCode());
        e.setToCode(row.getToCode());
        e.setType(row.getType());
        e.setWhenExpr(row.getWhenExpr());
        return e;
    }

    @Transactional
    public Long saveDraft(DefinitionSaveReq req) {
        log.info("micro-dag: 保存定义草稿 dagCode={} id={}", req.getDagCode(), req.getId());
        if (StringUtils.isBlank(req.getDagCode()) || StringUtils.isBlank(req.getDagName())) {
            throw ValidationException.of("dagCode / dagName 不能为空");
        }
        DagDefModel def = new DagDefModel();
        def.setDagCode(req.getDagCode());
        def.setDagName(req.getDagName());
        def.setNodes(req.getNodes() == null ? List.of() : req.getNodes());
        def.setEdges(req.getEdges() == null ? List.of() : req.getEdges());
        List<String> errors = validator.validate(def, registry.codes());
        if (!errors.isEmpty()) {
            throw ValidationException.of("定义校验失败: " + String.join("; ", errors));
        }

        DagDefinition entity;
        if (req.getId() == null) {
            entity = new DagDefinition();
            entity.setDagVersion(0);
            entity.setStatus(DefinitionStatus.DRAFT.name());
        } else {
            entity = selectById(req.getId());
            if (entity == null) {
                throw ValidationException.of("定义不存在: " + req.getId());
            }
            if (!DefinitionStatus.DRAFT.name().equals(entity.getStatus())) {
                throw ValidationException.of("仅 DRAFT 定义可编辑，已发布定义请复制为新版本");
            }
        }
        entity.setDagCode(req.getDagCode());
        entity.setDagName(req.getDagName());
        if (req.getId() == null) {
            insert(entity);
        } else {
            updateById(entity);
        }
        rebuildNodesAndEdges(entity, def);
        return entity.getId();
    }

    /** 重建该版本的节点/边行（草稿编辑全量覆盖，先删后插） */
    private void rebuildNodesAndEdges(DagDefinition entity, DagDefModel def) {
        DagNode nodeDel = new DagNode();
        nodeDel.setDagCode(entity.getDagCode());
        nodeDel.setDagVersion(entity.getDagVersion());
        for (DagNode old : nodeMapper.selectByEntity(nodeDel)) {
            nodeMapper.deleteById(old.getId());
        }
        DagEdge edgeDel = new DagEdge();
        edgeDel.setDagCode(entity.getDagCode());
        edgeDel.setDagVersion(entity.getDagVersion());
        for (DagEdge old : edgeMapper.selectByEntity(edgeDel)) {
            edgeMapper.deleteById(old.getId());
        }
        for (NodeDef n : def.getNodes()) {
            DagNode row = new DagNode();
            row.setDagCode(entity.getDagCode());
            row.setDagVersion(entity.getDagVersion());
            row.setNodeCode(n.getCode());
            row.setNodeName(n.getName());
            row.setHandler(n.getHandler());
            row.setTimeoutMs(n.getTimeoutMs());
            row.setMaxRetry(n.getMaxRetry());
            row.setIntervalSec(n.getIntervalSec());
            row.setBackoff(n.getBackoff());
            row.setParamsJson(toJson(n.getParams() == null ? Map.of() : n.getParams()));
            row.setPosX(n.getPosX());
            row.setPosY(n.getPosY());
            nodeMapper.insert(row);
        }
        for (EdgeDef e : def.getEdges()) {
            DagEdge row = new DagEdge();
            row.setDagCode(entity.getDagCode());
            row.setDagVersion(entity.getDagVersion());
            row.setEdgeCode(e.getEdgeCode());
            row.setFromCode(e.getFromCode());
            row.setToCode(e.getToCode());
            row.setType(e.getType() == null ? EdgeDef.TYPE_NORMAL : e.getType());
            row.setWhenExpr(e.getWhenExpr());
            edgeMapper.insert(row);
        }
    }

    /** 发布：全量校验 + 起始节点检查 + 版本递增，返回发布版本号 */
    @Transactional
    public Integer publish(Long id) {
        log.info("micro-dag: 发布定义 id={}", id);
        DagDefinition entity = selectById(id);
        if (entity == null) {
            throw ValidationException.of("定义不存在: " + id);
        }
        if (!DefinitionStatus.DRAFT.name().equals(entity.getStatus())) {
            throw ValidationException.of("仅 DRAFT 定义可发布");
        }
        DagDefModel def = load(entity.getDagCode(), entity.getDagVersion());
        List<String> errors = validator.validate(def, registry.codes());
        if (!errors.isEmpty()) {
            throw ValidationException.of("发布校验失败: " + String.join("; ", errors));
        }
        boolean hasStart = def.getNodes().stream().anyMatch(n ->
            def.getEdges() == null || def.getEdges().stream().noneMatch(e -> n.getCode().equals(e.getToCode())));
        if (!hasStart) {
            throw ValidationException.of("发布校验失败: 缺少起始节点（无上游的节点）");
        }

        DagDefinition query = new DagDefinition();
        query.setDagCode(entity.getDagCode());
        int maxVersion = selectByEntity(query).stream()
            .mapToInt(d -> d.getDagVersion() == null ? 0 : d.getDagVersion())
            .max().orElse(0);
        entity.setDagVersion(maxVersion + 1);
        entity.setStatus(DefinitionStatus.PUBLISHED.name());
        updateById(entity);
        log.info("micro-dag: 定义发布成功 dagCode={} version={}", entity.getDagCode(), entity.getDagVersion());
        return entity.getDagVersion();
    }

    public PageData<DagDefinition> page(DagDefinition query) {
        return selectPage(query);
    }

    /** 定义详情：元信息 + 节点/边行组装 */
    public DefinitionResp detail(Long id) {
        log.info("micro-dag: 查询定义详情 id={}", id);
        DagDefinition entity = selectById(id);
        if (entity == null) {
            throw ValidationException.of("定义不存在: " + id);
        }
        DagDefModel def = load(entity.getDagCode(), entity.getDagVersion());
        DefinitionResp resp = new DefinitionResp();
        BeanUtils.copyProperties(entity, resp);
        resp.setNodes(def.getNodes());
        resp.setEdges(def.getEdges());
        return resp;
    }

    /** 复制为新草稿（复制节点/边行，版本归零），返回新草稿 id */
    @Transactional
    public Long copyAsNewVersion(Long id) {
        log.info("micro-dag: 复制定义为新版本 id={}", id);
        DagDefinition src = selectById(id);
        if (src == null) {
            throw ValidationException.of("定义不存在: " + id);
        }
        DagDefModel srcDef = load(src.getDagCode(), src.getDagVersion());
        DagDefinition copy = new DagDefinition();
        copy.setDagCode(src.getDagCode());
        copy.setDagName(src.getDagName());
        copy.setDagVersion(0);
        copy.setStatus(DefinitionStatus.DRAFT.name());
        insert(copy);
        rebuildNodesAndEdges(copy, srcDef);
        return copy.getId();
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
