package com.wkclz.micro.dag.rest;

import com.wkclz.core.base.R;
import com.wkclz.micro.dag.bean.req.NodeInterveneReq;
import com.wkclz.micro.dag.bean.resp.NodeDetailResp;
import com.wkclz.micro.dag.service.DagInterventionService;
import com.wkclz.micro.dag.service.DagNodeInstanceService;
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
public class DagNodeInstanceRest {

    @Autowired
    private DagNodeInstanceService nodeInstanceService;
    @Autowired
    private DagInterventionService interventionService;

    @GetMapping(Route.NODE_DETAIL)
    public R<NodeDetailResp> detail(@RequestParam String instanceCode, @RequestParam String nodeCode) {
        return R.ok(nodeInstanceService.nodeDetail(instanceCode, nodeCode));
    }

    @PostMapping(Route.NODE_INTERRUPT)
    public R<Void> interrupt(@RequestParam String instanceCode, @RequestParam String nodeCode) {
        log.info("micro-dag: 中断节点 instanceCode={} nodeCode={}", instanceCode, nodeCode);
        interventionService.interrupt(instanceCode, nodeCode);
        return R.ok();
    }

    @PostMapping(Route.NODE_PARAMS)
    public R<Void> params(@RequestBody NodeInterveneReq req) {
        interventionService.params(req);
        return R.ok();
    }

    @PostMapping(Route.NODE_SKIP)
    public R<Void> skip(@RequestBody NodeInterveneReq req) {
        interventionService.skip(req);
        return R.ok();
    }

    @PostMapping(Route.NODE_RESET)
    public R<Void> reset(@RequestBody NodeInterveneReq req) {
        interventionService.reset(req);
        return R.ok();
    }

    @PostMapping(Route.NODE_RERUN)
    public R<Void> rerun(@RequestParam String instanceCode, @RequestParam String nodeCode) {
        log.info("micro-dag: 重跑节点 instanceCode={} nodeCode={}", instanceCode, nodeCode);
        interventionService.rerun(instanceCode, nodeCode);
        return R.ok();
    }
}
