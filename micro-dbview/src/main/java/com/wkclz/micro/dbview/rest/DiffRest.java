package com.wkclz.micro.dbview.rest;

import com.wkclz.core.base.R;
import com.wkclz.micro.dbview.bean.req.SchemaDiffReq;
import com.wkclz.micro.dbview.bean.resp.SchemaDiffResp;
import com.wkclz.micro.dbview.service.DbviewDiffService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "结构对比", description = "表结构对比接口")
@Validated
@RestController
@RequestMapping(Route.PREFIX)
public class DiffRest {

    @Autowired
    private DbviewDiffService diffService;

    @Operation(summary = "1. 结构对比-执行对比")
    @PostMapping(Route.DIFF_EXECUTE)
    public R<SchemaDiffResp> diff(@Valid @RequestBody SchemaDiffReq req) {
        return R.ok(diffService.diff(req));
    }
}
