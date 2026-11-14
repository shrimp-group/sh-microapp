package com.wkclz.micro.dag.rest;

import com.wkclz.core.base.R;
import com.wkclz.micro.dag.bean.req.LaunchReq;
import com.wkclz.micro.dag.service.DagInstanceService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping(Route.PREFIX)
public class DagInstanceRest {

    @Autowired
    private DagInstanceService instanceService;

    @PostMapping(Route.INSTANCE_LAUNCH)
    public R<Long> launch(@RequestBody LaunchReq req) {
        log.info("micro-dag: 发起实例 dagCode={} traceCode={}", req.getDagCode(), req.getTraceCode());
        return R.ok(instanceService.launch(req));
    }
}
