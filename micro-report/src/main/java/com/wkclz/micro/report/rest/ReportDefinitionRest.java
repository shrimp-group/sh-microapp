package com.wkclz.micro.report.rest;

import com.wkclz.core.base.PageData;
import com.wkclz.core.base.R;
import com.wkclz.micro.report.bean.dto.ReportDefinitionDto;
import com.wkclz.micro.report.bean.entity.ReportDefinition;
import com.wkclz.micro.report.bean.req.ReportDefinitionCreateReq;
import com.wkclz.micro.report.bean.req.ReportDefinitionInfoReq;
import com.wkclz.micro.report.bean.req.ReportDefinitionPageReq;
import com.wkclz.micro.report.bean.req.ReportDefinitionTestReq;
import com.wkclz.micro.report.bean.req.ReportDefinitionUpdateReq;
import com.wkclz.micro.report.bean.resp.ReportDefinitionPageResp;
import com.wkclz.micro.report.bean.resp.ReportDefinitionResp;
import com.wkclz.micro.report.service.ReportDefinitionService;
import com.wkclz.tool.utils.BeanUtil;
import com.wkclz.web.bean.RemoveReq;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Tag(name = "1.报表定义", description = "报表定义管理接口")
@Validated
@RestController
@RequestMapping(Route.PREFIX)
public class ReportDefinitionRest {

    @Autowired
    private ReportDefinitionService reportDefinitionService;

    @Operation(summary = "1.报表定义-分页查询", description = "根据条件分页查询报表定义列表")
    @GetMapping(Route.DEFINITION_PAGE)
    public R<PageData<ReportDefinitionPageResp>> definitionPage(@Valid ReportDefinitionPageReq req) {
        ReportDefinition entity = BeanUtil.cp(req, ReportDefinition.class);
        PageData<ReportDefinitionDto> page = reportDefinitionService.getDefinitionPage(entity);
        PageData<ReportDefinitionPageResp> newPage = page.convert(ReportDefinitionPageResp.class);
        return R.ok(newPage);
    }

    @Operation(summary = "2.报表定义-详情", description = "根据ID查询报表定义详情")
    @GetMapping(Route.DEFINITION_DETAIL)
    public R<ReportDefinitionResp> definitionDetail(@Valid ReportDefinitionInfoReq req) {
        ReportDefinition entity = reportDefinitionService.getDefinitionDetail(req.getId());
        if (entity == null) {
            return R.error("数据不存在");
        }
        ReportDefinitionResp resp = BeanUtil.cp(entity, ReportDefinitionResp.class);
        return R.ok(resp);
    }

    @Operation(summary = "3.报表定义-创建", description = "新增报表定义")
    @PostMapping(Route.DEFINITION_CREATE)
    @Transactional(rollbackFor = Exception.class)
    public R<ReportDefinitionResp> definitionCreate(@Valid @RequestBody ReportDefinitionCreateReq req) {
        ReportDefinition entity = BeanUtil.cp(req, ReportDefinition.class);
        entity = reportDefinitionService.definitionCreate(entity);
        ReportDefinitionResp resp = BeanUtil.cp(entity, ReportDefinitionResp.class);
        return R.ok(resp);
    }

    @Operation(summary = "4.报表定义-修改", description = "修改报表定义")
    @PostMapping(Route.DEFINITION_UPDATE)
    @Transactional(rollbackFor = Exception.class)
    public R<ReportDefinitionResp> definitionUpdate(@Valid @RequestBody ReportDefinitionUpdateReq req) {
        ReportDefinition entity = BeanUtil.cp(req, ReportDefinition.class);
        entity = reportDefinitionService.definitionUpdate(entity);
        ReportDefinitionResp resp = BeanUtil.cp(entity, ReportDefinitionResp.class);
        return R.ok(resp);
    }

    @Operation(summary = "5.报表定义-删除", description = "删除报表定义")
    @PostMapping(Route.DEFINITION_REMOVE)
    @Transactional(rollbackFor = Exception.class)
    public R<Integer> definitionRemove(@Valid @RequestBody RemoveReq req) {
        ReportDefinition entity = new ReportDefinition();
        entity.setId(req.getId());
        Integer result = reportDefinitionService.definitionRemove(entity);
        return R.ok(result);
    }

    @Operation(summary = "6.报表定义-SQL测试", description = "测试SQL脚本执行")
    @PostMapping(Route.DEFINITION_TEST)
    public R<Object> definitionTest(@Valid @RequestBody ReportDefinitionTestReq req) {
        Integer current = req.getCurrent() != null ? req.getCurrent() : 1;
        Integer size = req.getSize() != null ? req.getSize() : 10;
        Object result = reportDefinitionService.definitionTest(
            req.getReportCode(), req.getResultType(), req.getReportScript(),
            req.getReportScriptCount(), req.getReportScriptCountSwitch(),
            req.getReportScriptAutocamel(), current, size, req.getParams());
        return R.ok(result);
    }

}
