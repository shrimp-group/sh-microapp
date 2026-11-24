package com.wkclz.micro.dag.rest;

import com.wkclz.core.base.PageData;
import com.wkclz.core.base.R;
import com.wkclz.micro.dag.bean.entity.DagProcess;
import com.wkclz.micro.dag.bean.req.ProcessCreateReq;
import com.wkclz.micro.dag.bean.resp.ProcessCreateResp;
import com.wkclz.micro.dag.bean.resp.ProcessPageResp;
import com.wkclz.micro.dag.service.DagProcessService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "DAG-流程管理", description = "DAG 流程定义管理")
@Slf4j
@RestController
@RequestMapping(Route.PREFIX)
public class DagProcessRest {

    @Resource
    private DagProcessService service;

    @Operation(summary = "分页查询流程")
    @GetMapping(Route.PROCESS_PAGE)
    public R<PageData<ProcessPageResp>> page(DagProcess query) {
        return R.ok(service.page(query));
    }

    @Operation(summary = "新建流程")
    @PostMapping(Route.PROCESS_CREATE)
    public R<ProcessCreateResp> create(@RequestBody ProcessCreateReq req) {
        log.info("micro-dag: 新建流程 dagName={}", req.getDagName());
        return R.ok(service.create(req));
    }
}
