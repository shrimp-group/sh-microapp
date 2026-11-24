package com.wkclz.micro.dag.rest;

import com.wkclz.core.base.R;
import com.wkclz.micro.dag.bean.req.ProcessVersionCreateReq;
import com.wkclz.micro.dag.bean.req.ProcessVersionUpdateReq;
import com.wkclz.micro.dag.bean.resp.ProcessVersionDetailResp;
import com.wkclz.micro.dag.bean.resp.ProcessVersionResp;
import com.wkclz.micro.dag.service.DagProcessVersionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "DAG-流程版本", description = "DAG 流程版本草稿与发布")
@Slf4j
@RestController
@RequestMapping(Route.PREFIX)
public class DagProcessVersionRest {

    @Resource
    private DagProcessVersionService service;

    @Operation(summary = "创建版本草稿")
    @PostMapping(Route.PROCESS_VERSION_CREATE)
    public R<Long> create(@RequestBody ProcessVersionCreateReq req) {
        log.info("micro-dag: 创建草稿 dagCode={} dagName={}", req.getDagCode(), req.getDagName());
        return R.ok(service.createDraft(req));
    }

    @Operation(summary = "保存版本草稿")
    @PostMapping(Route.PROCESS_VERSION_UPDATE)
    public R<Long> update(@RequestBody ProcessVersionUpdateReq req) {
        log.info("micro-dag: 保存草稿 id={} dagName={}", req.getId(), req.getDagName());
        return R.ok(service.updateDraft(req));
    }

    @Operation(summary = "发布版本")
    @PostMapping(Route.PROCESS_VERSION_PUBLISH)
    public R<Integer> publish(@Parameter(description = "版本 ID") @RequestParam Long id) {
        log.info("micro-dag: 发布版本 id={}", id);
        return R.ok(service.publish(id));
    }

    @Operation(summary = "查询版本详情")
    @GetMapping(Route.PROCESS_VERSION_DETAIL)
    public R<ProcessVersionDetailResp> detail(@Parameter(description = "版本 ID") @RequestParam Long id) {
        return R.ok(service.detail(id));
    }

    @Operation(summary = "查询版本列表")
    @GetMapping(Route.PROCESS_VERSION_LIST)
    public R<List<ProcessVersionResp>> versionList(@Parameter(description = "流程编码") @RequestParam String dagCode) {
        return R.ok(service.versionList(dagCode));
    }
}
