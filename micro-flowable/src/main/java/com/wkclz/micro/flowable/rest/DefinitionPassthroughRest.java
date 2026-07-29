package com.wkclz.micro.flowable.rest;

import com.wkclz.core.base.PageData;
import com.wkclz.core.base.R;
import com.wkclz.flowable.client.bean.req.ProcessDefPageReq;
import com.wkclz.flowable.client.bean.req.ProcessDeployPageReq;
import com.wkclz.flowable.client.bean.resp.*;
import com.wkclz.micro.flowable.bean.enums.ErrorType;
import com.wkclz.micro.flowable.service.FlowableClientWrapper;
import com.wkclz.web.bean.IdReq;
import com.wkclz.web.bean.RemoveReq;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "管理端-透传查询", description = "透传 flowable 流程定义/部署记录查询")
@RestController
@RequestMapping(Route.PREFIX)
@Validated
public class DefinitionPassthroughRest {

    @Autowired
    private FlowableClientWrapper clientWrapper;

    @Operation(summary = "流程定义分页")
    @GetMapping(Route.ADMIN_DEFINITION_PAGE)
    public R<PageData<ProcessDefPageResp>> definitionPage(@Valid ProcessDefPageReq req) {
        return clientWrapper.call(ErrorType.QUERY_ERROR, "ProcessDefinitionClient#page", req,
                () -> clientWrapper.getClient().getDefinition().page(req));
    }

    @Operation(summary = "流程定义详情")
    @GetMapping(Route.ADMIN_DEFINITION_INFO)
    public R<ProcessDefResp> definitionInfo(@Valid IdReq req) {
        return clientWrapper.call(ErrorType.QUERY_ERROR, "ProcessDefinitionClient#info", req,
                () -> clientWrapper.getClient().getDefinition().info(req));
    }

    @Operation(summary = "流程定义列表")
    @GetMapping(Route.ADMIN_DEFINITION_LIST)
    public R<List<ProcessDefListResp>> definitionList() {
        return clientWrapper.call(ErrorType.QUERY_ERROR, "ProcessDefinitionClient#list", null,
                () -> clientWrapper.getClient().getDefinition().list());
    }

    @Operation(summary = "部署记录分页")
    @GetMapping(Route.ADMIN_DEPLOY_PAGE)
    public R<PageData<ProcessDeployPageResp>> deployPage(@Valid ProcessDeployPageReq req) {
        return clientWrapper.call(ErrorType.QUERY_ERROR, "ProcessDeployClient#page", req,
                () -> clientWrapper.getClient().getDeploy().page(req));
    }

    @Operation(summary = "删除部署记录")
    @PostMapping(Route.ADMIN_DEPLOY_REMOVE)
    public R<Integer> deployRemove(@Valid @RequestBody RemoveReq req) {
        return clientWrapper.call(ErrorType.DEPLOY_ERROR, "ProcessDeployClient#remove", req,
                () -> clientWrapper.getClient().getDeploy().remove(req));
    }
}
