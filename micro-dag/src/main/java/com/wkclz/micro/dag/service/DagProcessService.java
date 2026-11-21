package com.wkclz.micro.dag.service;

import com.wkclz.core.base.PageData;
import com.wkclz.core.exception.ValidationException;
import com.wkclz.micro.dag.bean.entity.DagProcess;
import com.wkclz.micro.dag.bean.entity.DagProcessVersion;
import com.wkclz.micro.dag.bean.enums.ProcessStatus;
import com.wkclz.micro.dag.bean.req.ProcessCreateReq;
import com.wkclz.micro.dag.bean.req.ProcessVersionCreateReq;
import com.wkclz.micro.dag.bean.resp.ProcessCreateResp;
import com.wkclz.micro.dag.bean.resp.ProcessPageResp;
import com.wkclz.micro.dag.mapper.DagProcessMapper;
import com.wkclz.micro.dag.mapper.DagProcessVersionMapper;
import com.wkclz.mybatis.service.BaseService;
import com.wkclz.redis.helper.RedisIdGenerator;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;

/**
 * 流程域：新建流程 / 分页（含版本状态聚合）
 */
@Slf4j
@Service
public class DagProcessService extends BaseService<DagProcess, DagProcessMapper> {

    /** 聚合状态：有草稿 */
    public static final String STATUS_EDITING = "EDITING";
    /** 聚合状态：无草稿有发布版本 */
    public static final String STATUS_PUBLISHED = "PUBLISHED";

    private final DagProcessVersionService versionService;
    private final DagProcessVersionMapper versionMapper;
    private final RedisIdGenerator idGenerator;

    public DagProcessService(DagProcessVersionService versionService, DagProcessVersionMapper versionMapper,
                             RedisIdGenerator idGenerator) {
        this.versionService = versionService;
        this.versionMapper = versionMapper;
        this.idGenerator = idGenerator;
    }

    /** 新建流程：主表插行 + 创建 v0 草稿（节点/边/校验由版本域统一处理） */
    @Transactional
    public ProcessCreateResp create(ProcessCreateReq req) {
        log.info("micro-dag: 新建流程 dagName={}", req.getDagName());
        if (StringUtils.isBlank(req.getDagName())) {
            throw ValidationException.of("dagName 不能为空");
        }
        DagProcess process = new DagProcess();
        process.setDagCode(idGenerator.generateBase32IdWithPrefix("dag_"));
        process.setDagName(req.getDagName());
        insert(process);

        ProcessVersionCreateReq versionReq = new ProcessVersionCreateReq();
        versionReq.setDagCode(process.getDagCode());
        versionReq.setDagName(req.getDagName());
        versionReq.setNodes(req.getNodes());
        versionReq.setEdges(req.getEdges());
        versionService.createDraft(versionReq);
        log.info("micro-dag: 流程创建成功 id={} dagCode={}", process.getId(), process.getDagCode());

        ProcessCreateResp resp = new ProcessCreateResp();
        resp.setId(process.getId());
        resp.setDagCode(process.getDagCode());
        return resp;
    }

    /** 流程分页：主表分页后按 dagCode 聚合版本状态（EDITING/PUBLISHED） */
    public PageData<ProcessPageResp> page(DagProcess query) {
        log.info("micro-dag: 流程分页 dagCode={} dagName={}", query.getDagCode(), query.getDagName());
        PageData<DagProcess> page = selectPage(query);
        List<ProcessPageResp> rows = page.getRecords().stream().map(p -> {
            ProcessPageResp resp = new ProcessPageResp();
            resp.setId(p.getId());
            resp.setDagCode(p.getDagCode());
            resp.setDagName(p.getDagName());
            resp.setUpdateTime(p.getUpdateTime());
            return resp;
        }).toList();
        for (ProcessPageResp resp : rows) {
            DagProcessVersion q = new DagProcessVersion();
            q.setDagCode(resp.getDagCode());
            List<DagProcessVersion> versions = versionMapper.selectByEntity(q);
            versions.stream()
                .filter(v -> ProcessStatus.DRAFT.name().equals(v.getStatus()))
                .max(Comparator.comparingInt(DagProcessVersion::getDagVersion))
                .ifPresent(d -> {
                    resp.setStatus(STATUS_EDITING);
                    resp.setDraftId(d.getId());
                });
            versions.stream()
                .filter(v -> ProcessStatus.PUBLISHED.name().equals(v.getStatus()))
                .max(Comparator.comparingInt(DagProcessVersion::getDagVersion))
                .ifPresent(d -> {
                    resp.setLatestVersion(d.getDagVersion());
                    resp.setLatestPublishedId(d.getId());
                });
            // 无草稿：有发布版本或全新流程（连发布版本也没有）均按已发布展示
            if (resp.getStatus() == null) {
                resp.setStatus(STATUS_PUBLISHED);
            }
        }
        return PageData.convert(page, rows);
    }
}
