package com.wkclz.micro.dag.rest;

import com.wkclz.core.base.R;
import com.wkclz.micro.dag.bean.req.NodeInterveneReq;
import com.wkclz.micro.dag.bean.resp.NodeDetailResp;
import com.wkclz.micro.dag.service.DagInterventionService;
import com.wkclz.micro.dag.service.DagNodeInstanceService;
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

@Tag(name = "DAG-节点干预", description = "DAG 节点实例查询与人工干预")
@Slf4j
@RestController
@RequestMapping(Route.PREFIX)
public class DagNodeInstanceRest {

    @Resource
    private DagNodeInstanceService nodeInstanceService;
    @Resource
    private DagInterventionService interventionService;

    @Operation(summary = "查询节点详情")
    @GetMapping(Route.NODE_DETAIL)
    public R<NodeDetailResp> detail(@Parameter(description = "实例编码") @RequestParam String instanceCode,
                                    @Parameter(description = "节点编码") @RequestParam String nodeCode) {
        return R.ok(nodeInstanceService.nodeDetail(instanceCode, nodeCode));
    }

    @Operation(summary = "中断节点")
    @PostMapping(Route.NODE_INTERRUPT)
    public R<Void> interrupt(@Parameter(description = "实例编码") @RequestParam String instanceCode,
                             @Parameter(description = "节点编码") @RequestParam String nodeCode) {
        log.info("micro-dag: 中断节点 instanceCode={} nodeCode={}", instanceCode, nodeCode);
        interventionService.interrupt(instanceCode, nodeCode);
        return R.ok();
    }

    @Operation(summary = "覆盖节点入参")
    @PostMapping(Route.NODE_PARAMS)
    public R<Void> params(@RequestBody NodeInterveneReq req) {
        log.info("micro-dag: 覆盖节点入参 instanceCode={} nodeCode={}", req.getInstanceCode(), req.getNodeCode());
        interventionService.params(req);
        return R.ok();
    }

    @Operation(summary = "跳过节点")
    @PostMapping(Route.NODE_SKIP)
    public R<Void> skip(@RequestBody NodeInterveneReq req) {
        log.info("micro-dag: 跳过节点 instanceCode={} nodeCode={}", req.getInstanceCode(), req.getNodeCode());
        interventionService.skip(req);
        return R.ok();
    }

    @Operation(summary = "重置节点")
    @PostMapping(Route.NODE_RESET)
    public R<Void> reset(@RequestBody NodeInterveneReq req) {
        log.info("micro-dag: 重置节点 instanceCode={} nodeCode={}", req.getInstanceCode(), req.getNodeCode());
        interventionService.reset(req);
        return R.ok();
    }

    @Operation(summary = "重跑节点")
    @PostMapping(Route.NODE_RERUN)
    public R<Void> rerun(@RequestBody NodeInterveneReq req) {
        log.info("micro-dag: 重跑节点 instanceCode={} nodeCode={} hasParams={}",
            req.getInstanceCode(), req.getNodeCode(), StringUtils.isNotBlank(req.getInputParamsJson()));
        interventionService.rerun(req);
        return R.ok();
    }
}
