package com.wkclz.micro.report.rest;

import com.wkclz.core.base.R;
import com.wkclz.micro.report.bean.dto.ReportDefinitionParamDto;
import com.wkclz.micro.report.bean.entity.ReportDefinitionParam;
import com.wkclz.micro.report.bean.req.ReportDefinitionParamCreateReq;
import com.wkclz.micro.report.bean.req.ReportDefinitionParamExtractReq;
import com.wkclz.micro.report.bean.req.ReportDefinitionParamListReq;
import com.wkclz.micro.report.bean.req.ReportDefinitionParamUpdateReq;
import com.wkclz.micro.report.bean.resp.ReportDefinitionParamResp;
import com.wkclz.micro.report.service.ReportDefinitionParamService;
import com.wkclz.tool.utils.BeanUtil;
import com.wkclz.web.bean.RemoveReq;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "3.报表参数", description = "报表参数管理接口")
@Validated
@RestController
@RequestMapping(Route.PREFIX)
public class ReportDefinitionParamRest {

    @Autowired
    private ReportDefinitionParamService reportDefinitionParamService;

    @Operation(summary = "1.报表参数-列表查询", description = "根据报表编码查询参数列表")
    @GetMapping(Route.DEFINITION_PARAM_LIST)
    public R<List<ReportDefinitionParamResp>> paramList(@Valid ReportDefinitionParamListReq req) {
        ReportDefinitionParam param = BeanUtil.cp(req, ReportDefinitionParam.class);
        List<ReportDefinitionParam> list = reportDefinitionParamService.getParamList(param);
        List<ReportDefinitionParamResp> respList = BeanUtil.cp(list, ReportDefinitionParamResp.class);
        return R.ok(respList);
    }

    @Operation(summary = "2.报表参数-创建", description = "新增报表参数")
    @PostMapping(Route.DEFINITION_PARAM_CREATE)
    @Transactional(rollbackFor = Exception.class)
    public R<ReportDefinitionParamResp> paramCreate(@Valid @RequestBody ReportDefinitionParamCreateReq req) {
        ReportDefinitionParam entity = BeanUtil.cp(req, ReportDefinitionParam.class);
        entity = reportDefinitionParamService.paramCreate(entity);
        ReportDefinitionParamResp resp = BeanUtil.cp(entity, ReportDefinitionParamResp.class);
        return R.ok(resp);
    }

    @Operation(summary = "3.报表参数-修改", description = "修改报表参数")
    @PostMapping(Route.DEFINITION_PARAM_UPDATE)
    @Transactional(rollbackFor = Exception.class)
    public R<ReportDefinitionParamResp> paramUpdate(@Valid @RequestBody ReportDefinitionParamUpdateReq req) {
        ReportDefinitionParam entity = BeanUtil.cp(req, ReportDefinitionParam.class);
        entity = reportDefinitionParamService.paramUpdate(entity);
        ReportDefinitionParamResp resp = BeanUtil.cp(entity, ReportDefinitionParamResp.class);
        return R.ok(resp);
    }

    @Operation(summary = "4.报表参数-删除", description = "删除报表参数")
    @PostMapping(Route.DEFINITION_PARAM_REMOVE)
    @Transactional(rollbackFor = Exception.class)
    public R<Integer> paramRemove(@Valid @RequestBody RemoveReq req) {
        ReportDefinitionParam entity = new ReportDefinitionParam();
        entity.setId(req.getId());
        Integer result = reportDefinitionParamService.paramRemove(entity);
        return R.ok(result);
    }

    @Operation(summary = "5.报表参数-自动提取", description = "从SQL脚本自动提取参数")
    @PostMapping(Route.DEFINITION_PARAM_EXTRACT)
    public R<List<ReportDefinitionParamResp>> paramExtract(@Valid @RequestBody ReportDefinitionParamExtractReq req) {
        ReportDefinitionParamDto dto = BeanUtil.cp(req, ReportDefinitionParamDto.class);
        List<ReportDefinitionParam> list = reportDefinitionParamService.paramsExtract(dto);
        List<ReportDefinitionParamResp> respList = BeanUtil.cp(list, ReportDefinitionParamResp.class);
        return R.ok(respList);
    }

}
