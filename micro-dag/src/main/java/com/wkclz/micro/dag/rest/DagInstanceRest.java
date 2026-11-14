package com.wkclz.micro.dag.rest;

import com.wkclz.core.base.PageData;
import com.wkclz.core.base.R;
import com.wkclz.micro.dag.bean.req.InstancePageReq;
import com.wkclz.micro.dag.bean.req.LaunchReq;
import com.wkclz.micro.dag.bean.resp.InstanceDagViewResp;
import com.wkclz.micro.dag.bean.resp.InstanceResp;
import com.wkclz.micro.dag.service.DagInstanceService;
import com.wkclz.micro.dag.service.DagInterventionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping(Route.PREFIX)
public class DagInstanceRest {

    @Autowired
    private DagInstanceService instanceService;
    @Autowired
    private DagInterventionService interventionService;

    @PostMapping(Route.INSTANCE_LAUNCH)
    public R<Long> launch(@RequestBody LaunchReq req) {
        log.info("micro-dag: 发起实例 dagCode={} traceCode={}", req.getDagCode(), req.getTraceCode());
        return R.ok(instanceService.launch(req));
    }

    @GetMapping(Route.INSTANCE_PAGE)
    public R<PageData<InstanceResp>> page(InstancePageReq req) {
        return R.ok(instanceService.page(req));
    }

    @GetMapping(Route.INSTANCE_DETAIL)
    public R<InstanceDagViewResp> detail(@RequestParam String instanceCode) {
        return R.ok(instanceService.detail(instanceCode));
    }

    @PostMapping(Route.INSTANCE_TERMINATE)
    public R<Void> terminate(@RequestParam String instanceCode) {
        log.info("micro-dag: 终止实例 instanceCode={}", instanceCode);
        interventionService.terminate(instanceCode);
        return R.ok();
    }

    @PostMapping(Route.INSTANCE_RERUN_FROM)
    public R<Void> rerunFrom(@RequestParam String instanceCode, @RequestParam String nodeCode) {
        log.info("micro-dag: 从指定节点重跑 instanceCode={} nodeCode={}", instanceCode, nodeCode);
        interventionService.rerunFrom(instanceCode, nodeCode);
        return R.ok();
    }
}
