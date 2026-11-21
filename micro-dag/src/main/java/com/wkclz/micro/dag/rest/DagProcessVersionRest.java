package com.wkclz.micro.dag.rest;

import com.wkclz.core.base.R;
import com.wkclz.micro.dag.bean.req.ProcessVersionCreateReq;
import com.wkclz.micro.dag.bean.req.ProcessVersionUpdateReq;
import com.wkclz.micro.dag.bean.resp.ProcessVersionDetailResp;
import com.wkclz.micro.dag.bean.resp.ProcessVersionResp;
import com.wkclz.micro.dag.service.DagProcessVersionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequestMapping(Route.PREFIX)
public class DagProcessVersionRest {

    @Autowired
    private DagProcessVersionService service;

    @PostMapping(Route.PROCESS_VERSION_CREATE)
    public R<Long> create(@RequestBody ProcessVersionCreateReq req) {
        log.info("micro-dag: 创建草稿 dagCode={} dagName={}", req.getDagCode(), req.getDagName());
        return R.ok(service.createDraft(req));
    }

    @PostMapping(Route.PROCESS_VERSION_UPDATE)
    public R<Long> update(@RequestBody ProcessVersionUpdateReq req) {
        log.info("micro-dag: 保存草稿 id={} dagName={}", req.getId(), req.getDagName());
        return R.ok(service.updateDraft(req));
    }

    @PostMapping(Route.PROCESS_VERSION_PUBLISH)
    public R<Integer> publish(@RequestParam Long id) {
        log.info("micro-dag: 发布版本 id={}", id);
        return R.ok(service.publish(id));
    }

    @GetMapping(Route.PROCESS_VERSION_DETAIL)
    public R<ProcessVersionDetailResp> detail(@RequestParam Long id) {
        return R.ok(service.detail(id));
    }

    @GetMapping(Route.PROCESS_VERSION_LIST)
    public R<List<ProcessVersionResp>> versionList(@RequestParam String dagCode) {
        return R.ok(service.versionList(dagCode));
    }
}
