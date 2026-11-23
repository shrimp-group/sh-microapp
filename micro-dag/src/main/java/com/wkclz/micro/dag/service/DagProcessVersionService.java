package com.wkclz.micro.dag.service;

import com.wkclz.core.exception.ValidationException;
import com.wkclz.micro.dag.api.NodeHandlerRegistry;
import com.wkclz.micro.dag.bean.dto.DagDefModel;
import com.wkclz.micro.dag.bean.dto.EdgeDef;
import com.wkclz.micro.dag.bean.dto.NodeDef;
import com.wkclz.micro.dag.bean.entity.DagProcess;
import com.wkclz.micro.dag.bean.entity.DagProcessVersion;
import com.wkclz.micro.dag.bean.entity.DagEdge;
import com.wkclz.micro.dag.bean.entity.DagNode;
import com.wkclz.micro.dag.bean.enums.NodeType;
import com.wkclz.micro.dag.bean.enums.ProcessStatus;
import com.wkclz.micro.dag.bean.req.ProcessVersionCreateReq;
import com.wkclz.micro.dag.bean.req.ProcessVersionUpdateReq;
import com.wkclz.micro.dag.bean.resp.ProcessVersionDetailResp;
import com.wkclz.micro.dag.bean.resp.ProcessVersionResp;
import com.wkclz.micro.dag.core.DagValidator;
import com.wkclz.micro.dag.mapper.DagEdgeMapper;
import com.wkclz.micro.dag.mapper.DagNodeMapper;
import com.wkclz.micro.dag.mapper.DagProcessMapper;
import com.wkclz.micro.dag.mapper.DagProcessVersionMapper;
import com.wkclz.mybatis.service.BaseService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

import java.util.Comparator;
import java.util.List;
import java.util.Map;

/**
 * 流程版本域：load（运行时装配共用入口）/ createDraft / updateDraft / publish / detail / versionList
 */
@Slf4j
@Service
public class DagProcessVersionService extends BaseService<DagProcessVersion, DagProcessVersionMapper> {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private final DagValidator validator;
    private final NodeHandlerRegistry registry;
    private final DagNodeMapper nodeMapper;
    private final DagEdgeMapper edgeMapper;
    private final DagProcessMapper processMapper;

    public DagProcessVersionService(DagValidator validator, NodeHandlerRegistry registry,
                                    DagNodeMapper nodeMapper, DagEdgeMapper edgeMapper,
                                    DagProcessMapper processMapper) {
        this.validator = validator;
        this.registry = registry;
        this.nodeMapper = nodeMapper;
        this.edgeMapper = edgeMapper;
        this.processMapper = processMapper;
    }

    /** 按版本查 dag_node/dag_edge 组装 DagDefModel（定义详情/引擎装配共用入口），版本不存在抛 ValidationException */
    public DagDefModel load(String dagCode, Integer dagVersion) {
        log.info("micro-dag: 加载版本 dagCode={} dagVersion={}", dagCode, dagVersion);
        DagProcessVersion versionQuery = new DagProcessVersion();
        versionQuery.setDagCode(dagCode);
        versionQuery.setDagVersion(dagVersion);
        DagProcessVersion meta = selectOneByEntity(versionQuery);
        if (meta == null) {
            throw ValidationException.of("版本不存在: " + dagCode + " v" + dagVersion);
        }
        DagNode nodeQuery = new DagNode();
        nodeQuery.setDagCode(dagCode);
        nodeQuery.setDagVersion(dagVersion);
        List<DagNode> nodeRows = nodeMapper.selectByEntity(nodeQuery);
        if (nodeRows == null || nodeRows.isEmpty()) {
            throw ValidationException.of("版本无节点: " + dagCode + " v" + dagVersion);
        }
        DagEdge edgeQuery = new DagEdge();
        edgeQuery.setDagCode(dagCode);
        edgeQuery.setDagVersion(dagVersion);
        List<DagEdge> edgeRows = edgeMapper.selectByEntity(edgeQuery);

        DagDefModel def = new DagDefModel();
        def.setDagCode(meta.getDagCode());
        // 名称唯一持有于主表，历史版本显示当前名称
        def.setDagName(loadName(dagCode));
        def.setDagVersion(meta.getDagVersion());
        def.setNodes(nodeRows.stream().map(this::toNodeDef).toList());
        def.setEdges(edgeRows == null ? List.of() : edgeRows.stream().map(this::toEdgeDef).toList());
        return def;
    }

    private String loadName(String dagCode) {
        DagProcess q = new DagProcess();
        q.setDagCode(dagCode);
        DagProcess process = processMapper.selectOneByEntity(q);
        return process == null ? null : process.getDagName();
    }

    /** 创建草稿：基于提交内容落 v0 草稿并同步主表名称；流程不存在/已有草稿抛 ValidationException */
    @Transactional
    public Long createDraft(ProcessVersionCreateReq req) {
        log.info("micro-dag: 创建草稿 dagCode={} dagName={}", req.getDagCode(), req.getDagName());
        if (StringUtils.isBlank(req.getDagCode())) {
            throw ValidationException.of("dagCode 不能为空");
        }
        if (StringUtils.isBlank(req.getDagName())) {
            throw ValidationException.of("dagName 不能为空");
        }
        if (loadProcess(req.getDagCode()) == null) {
            throw ValidationException.of("流程不存在: " + req.getDagCode());
        }
        DagDefModel def = buildAndValidateDef(req.getDagName(), req.getNodes(), req.getEdges());

        DagProcessVersion draft = new DagProcessVersion();
        draft.setDagCode(req.getDagCode());
        draft.setDagVersion(0);
        draft.setStatus(ProcessStatus.DRAFT.name());
        try {
            insert(draft);
        } catch (DuplicateKeyException e) {
            // uk_code_version 唯一索引硬约束：并发创建草稿
            log.error("micro-dag: 并发创建草稿冲突 dagCode={}", req.getDagCode(), e);
            throw ValidationException.of("已有编辑中草稿，请刷新列表");
        }
        syncName(req.getDagCode(), req.getDagName());
        rebuildNodesAndEdges(draft, def);
        log.info("micro-dag: 草稿创建成功 id={} dagCode={}", draft.getId(), draft.getDagCode());
        return draft.getId();
    }

    /** 保存草稿：仅 DRAFT 可改，覆盖节点/边行并同步主表名称，返回版本 id */
    @Transactional
    public Long updateDraft(ProcessVersionUpdateReq req) {
        log.info("micro-dag: 保存草稿 id={} dagName={}", req.getId(), req.getDagName());
        if (req.getId() == null) {
            throw ValidationException.of("id 不能为空");
        }
        DagDefModel def = buildAndValidateDef(req.getDagName(), req.getNodes(), req.getEdges());
        DagProcessVersion entity = selectById(req.getId());
        if (entity == null) {
            throw ValidationException.of("版本不存在: " + req.getId());
        }
        if (!ProcessStatus.DRAFT.name().equals(entity.getStatus())) {
            throw ValidationException.of("仅 DRAFT 版本可修改");
        }
        syncName(entity.getDagCode(), req.getDagName());
        rebuildNodesAndEdges(entity, def);
        return entity.getId();
    }

    private DagProcess loadProcess(String dagCode) {
        DagProcess q = new DagProcess();
        q.setDagCode(dagCode);
        return processMapper.selectOneByEntity(q);
    }

    /** 同步主表名称（先查后改，命中乐观锁更新；名称相同跳过） */
    private void syncName(String dagCode, String dagName) {
        DagProcess process = loadProcess(dagCode);
        if (process == null) {
            throw ValidationException.of("流程不存在: " + dagCode);
        }
        if (dagName.equals(process.getDagName())) {
            return;
        }
        process.setDagName(dagName);
        processMapper.updateById(process);
    }

    /** 发布：全量校验 + 版本递增重挂，节点/边行随版本重挂，返回发布版本号 */
    @Transactional
    public Integer publish(Long id) {
        log.info("micro-dag: 发布版本 id={}", id);
        DagProcessVersion entity = selectById(id);
        if (entity == null) {
            throw ValidationException.of("版本不存在: " + id);
        }
        if (!ProcessStatus.DRAFT.name().equals(entity.getStatus())) {
            throw ValidationException.of("仅 DRAFT 版本可发布");
        }
        DagDefModel def = load(entity.getDagCode(), entity.getDagVersion());
        List<String> errors = validator.validate(def, registry);
        if (!errors.isEmpty()) {
            throw ValidationException.of("发布校验失败: " + String.join("; ", errors));
        }

        DagProcessVersion query = new DagProcessVersion();
        query.setDagCode(entity.getDagCode());
        int maxVersion = selectByEntity(query).stream()
            .mapToInt(d -> d.getDagVersion() == null ? 0 : d.getDagVersion())
            .max().orElse(0);
        // 节点/边行仍挂在旧版本上：先删旧版本行，版本号提升后按新版本重建，保证 load 新版本可查
        deleteNodesAndEdges(entity.getDagCode(), entity.getDagVersion());
        entity.setDagVersion(maxVersion + 1);
        entity.setStatus(ProcessStatus.PUBLISHED.name());
        updateById(entity);
        rebuildNodesAndEdges(entity, def);
        log.info("micro-dag: 版本发布成功 dagCode={} version={}", entity.getDagCode(), entity.getDagVersion());
        return entity.getDagVersion();
    }

    /** 版本详情：主表名称 + 版本元信息 + 节点/边行组装 */
    public ProcessVersionDetailResp detail(Long id) {
        log.info("micro-dag: 查询版本详情 id={}", id);
        DagProcessVersion entity = selectById(id);
        if (entity == null) {
            throw ValidationException.of("版本不存在: " + id);
        }
        DagDefModel def = load(entity.getDagCode(), entity.getDagVersion());
        ProcessVersionDetailResp resp = new ProcessVersionDetailResp();
        resp.setId(entity.getId());
        resp.setDagCode(entity.getDagCode());
        resp.setDagName(def.getDagName());
        resp.setDagVersion(entity.getDagVersion());
        resp.setStatus(entity.getStatus());
        resp.setNodes(def.getNodes());
        resp.setEdges(def.getEdges());
        return resp;
    }

    /** 版本列表（历史版本页）：按 dagVersion 倒序 */
    public List<ProcessVersionResp> versionList(String dagCode) {
        log.info("micro-dag: 查询版本列表 dagCode={}", dagCode);
        DagProcessVersion q = new DagProcessVersion();
        q.setDagCode(dagCode);
        List<DagProcessVersion> rows = selectByEntity(q);
        return rows.stream()
            .map(r -> {
                ProcessVersionResp resp = new ProcessVersionResp();
                resp.setId(r.getId());
                resp.setDagCode(r.getDagCode());
                resp.setDagVersion(r.getDagVersion());
                resp.setStatus(r.getStatus());
                resp.setUpdateTime(r.getUpdateTime());
                return resp;
            })
            .sorted(Comparator.comparingInt(ProcessVersionResp::getDagVersion).reversed())
            .toList();
    }

    private NodeDef toNodeDef(DagNode row) {
        NodeDef n = new NodeDef();
        n.setCode(row.getNodeCode());
        n.setName(row.getNodeName());
        n.setNodeType(row.getNodeType());
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

    /** 组装 DagDefModel 并做全量校验，校验失败抛 ValidationException */
    private DagDefModel buildAndValidateDef(String dagName, List<NodeDef> nodes, List<EdgeDef> edges) {
        DagDefModel def = new DagDefModel();
        def.setDagName(dagName);
        def.setNodes(nodes == null ? List.of() : nodes);
        def.setEdges(edges == null ? List.of() : edges);
        List<String> errors = validator.validate(def, registry);
        if (!errors.isEmpty()) {
            throw ValidationException.of("定义校验失败: " + String.join("; ", errors));
        }
        return def;
    }

    /** 重建该版本的节点/边行（草稿编辑全量覆盖，先删后插） */
    private void rebuildNodesAndEdges(DagProcessVersion entity, DagDefModel def) {
        deleteNodesAndEdges(entity.getDagCode(), entity.getDagVersion());
        for (NodeDef n : def.getNodes()) {
            DagNode row = new DagNode();
            row.setDagCode(entity.getDagCode());
            row.setDagVersion(entity.getDagVersion());
            row.setNodeCode(n.getCode());
            row.setNodeName(n.getName());
            row.setNodeType(n.getNodeType() == null ? NodeType.NORMAL.getCode() : n.getNodeType());
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

    /** 删除指定版本的节点/边行（版本重挂与草稿覆盖共用） */
    private void deleteNodesAndEdges(String dagCode, Integer dagVersion) {
        DagNode nodeDel = new DagNode();
        nodeDel.setDagCode(dagCode);
        nodeDel.setDagVersion(dagVersion);
        for (DagNode old : nodeMapper.selectByEntity(nodeDel)) {
            nodeMapper.deleteById(old.getId());
        }
        DagEdge edgeDel = new DagEdge();
        edgeDel.setDagCode(dagCode);
        edgeDel.setDagVersion(dagVersion);
        for (DagEdge old : edgeMapper.selectByEntity(edgeDel)) {
            edgeMapper.deleteById(old.getId());
        }
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
