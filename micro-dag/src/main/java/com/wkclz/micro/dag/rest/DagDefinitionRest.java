package com.wkclz.micro.dag.rest;

import com.wkclz.core.base.PageData;
import com.wkclz.core.base.R;
import com.wkclz.micro.dag.bean.entity.DagDefinition;
import com.wkclz.micro.dag.bean.req.DefinitionSaveReq;
import com.wkclz.micro.dag.bean.resp.DefinitionResp;
import com.wkclz.micro.dag.service.DagDefinitionService;
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
public class DagDefinitionRest {

    @Autowired
    private DagDefinitionService service;

    @PostMapping(Route.DEFINITION_SAVE)
    public R<Long> save(@RequestBody DefinitionSaveReq req) {
        log.info("micro-dag: 保存定义 dagCode={} id={}", req.getDagCode(), req.getId());
        return R.ok(service.saveDraft(req));
    }

    @PostMapping(Route.DEFINITION_PUBLISH)
    public R<Integer> publish(@RequestParam Long id) {
        log.info("micro-dag: 发布定义 id={}", id);
        return R.ok(service.publish(id));
    }

    @GetMapping(Route.DEFINITION_PAGE)
    public R<PageData<DagDefinition>> page(DagDefinition query) {
        return R.ok(service.page(query));
    }

    @GetMapping(Route.DEFINITION_DETAIL)
    public R<DefinitionResp> detail(@RequestParam Long id) {
        return R.ok(service.detail(id));
    }

    @PostMapping(Route.DEFINITION_COPY)
    public R<Long> copy(@RequestParam Long id) {
        log.info("micro-dag: 复制定义 id={}", id);
        return R.ok(service.copyAsNewVersion(id));
    }
}
