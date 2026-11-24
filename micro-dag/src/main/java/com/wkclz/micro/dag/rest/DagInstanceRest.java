package com.wkclz.micro.dag.rest;

import com.wkclz.core.base.PageData;
import com.wkclz.core.base.R;
import com.wkclz.micro.dag.bean.req.InstancePageReq;
import com.wkclz.micro.dag.bean.req.InstanceRerunFromReq;
import com.wkclz.micro.dag.bean.req.LaunchReq;
import com.wkclz.micro.dag.bean.resp.InstanceDagViewResp;
import com.wkclz.micro.dag.bean.resp.InstanceResp;
import com.wkclz.micro.dag.service.DagInstanceService;
import com.wkclz.micro.dag.service.DagInterventionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "DAG-流程实例", description = "DAG 流程实例发起、查询与干预")
@Slf4j
@RestController
@RequestMapping(Route.PREFIX)
public class DagInstanceRest {

    @Resource
    private DagInstanceService instanceService;
    @Resource
    private DagInterventionService interventionService;

    @Operation(summary = "发起流程实例")
    @PostMapping(Route.INSTANCE_LAUNCH)
    public R<Long> launch(@RequestBody LaunchReq req) {
        log.info("micro-dag: 发起实例 dagCode={} traceCode={}", req.getDagCode(), req.getTraceCode());
        return R.ok(instanceService.launch(req));
    }

    @Operation(summary = "分页查询实例")
    @GetMapping(Route.INSTANCE_PAGE)
    public R<PageData<InstanceResp>> page(InstancePageReq req) {
        return R.ok(instanceService.page(req));
    }

    @Operation(summary = "查询实例详情（DAG 视图）")
    @GetMapping(Route.INSTANCE_DETAIL)
    public R<InstanceDagViewResp> detail(@Parameter(description = "实例编码") @RequestParam String instanceCode) {
        return R.ok(instanceService.detail(instanceCode));
    }

    @Operation(summary = "终止实例")
    @PostMapping(Route.INSTANCE_TERMINATE)
    public R<Void> terminate(@Parameter(description = "实例编码") @RequestParam String instanceCode) {
        log.info("micro-dag: 终止实例 instanceCode={}", instanceCode);
        interventionService.terminate(instanceCode);
        return R.ok();
    }

    @Operation(summary = "从指定节点重跑实例")
    @PostMapping(Route.INSTANCE_RERUN_FROM)
    public R<Void> rerunFrom(@RequestBody InstanceRerunFromReq req) {
        log.info("micro-dag: 从指定节点重跑 instanceCode={} nodeCode={} hasParams={}",
            req.getInstanceCode(), req.getNodeCode(), StringUtils.isNotBlank(req.getInstanceParamsJson()));
        interventionService.rerunFrom(req);
        return R.ok();
    }
}
