package com.wkclz.micro.dag.rest;

import com.wkclz.core.base.PageData;
import com.wkclz.core.base.R;
import com.wkclz.micro.dag.bean.entity.DagProcess;
import com.wkclz.micro.dag.bean.req.ProcessCreateReq;
import com.wkclz.micro.dag.bean.resp.ProcessCreateResp;
import com.wkclz.micro.dag.bean.resp.ProcessPageResp;
import com.wkclz.micro.dag.service.DagProcessService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping(Route.PREFIX)
public class DagProcessRest {

    @Autowired
    private DagProcessService service;

    @GetMapping(Route.PROCESS_PAGE)
    public R<PageData<ProcessPageResp>> page(DagProcess query) {
        return R.ok(service.page(query));
    }

    @PostMapping(Route.PROCESS_CREATE)
    public R<ProcessCreateResp> create(@RequestBody ProcessCreateReq req) {
        log.info("micro-dag: 新建流程 dagName={}", req.getDagName());
        return R.ok(service.create(req));
    }
}
