package com.wkclz.micro.report.rest;

import com.wkclz.core.base.R;
import com.wkclz.micro.report.bean.dto.ReportDefinitionResultDto;
import com.wkclz.micro.report.bean.entity.ReportDefinitionParam;
import com.wkclz.micro.report.bean.entity.ReportDefinitionResult;
import com.wkclz.micro.report.bean.req.ReportDefinitionResultCreateReq;
import com.wkclz.micro.report.bean.req.ReportDefinitionResultExtractReq;
import com.wkclz.micro.report.bean.req.ReportDefinitionResultListReq;
import com.wkclz.micro.report.bean.req.ReportDefinitionResultUpdateReq;
import com.wkclz.micro.report.bean.resp.ReportDefinitionResultResp;
import com.wkclz.micro.report.service.ReportDefinitionResultService;
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

@Tag(name = "4.报表结果字段", description = "报表结果字段管理接口")
@Validated
@RestController
@RequestMapping(Route.PREFIX)
public class ReportDefinitionResultRest {

    @Autowired
    private ReportDefinitionResultService reportDefinitionResultService;

    @Operation(summary = "1.报表结果字段-列表查询", description = "根据报表编码查询结果字段列表")
    @GetMapping(Route.DEFINITION_RESULT_LIST)
    public R<List<ReportDefinitionResultResp>> resultList(@Valid ReportDefinitionResultListReq req) {
        ReportDefinitionResult result = BeanUtil.cp(req, ReportDefinitionResult.class);
        List<ReportDefinitionResult> list = reportDefinitionResultService.getResultList(result);
        List<ReportDefinitionResultResp> respList = BeanUtil.cp(list, ReportDefinitionResultResp.class);
        return R.ok(respList);
    }

    @Operation(summary = "2.报表结果字段-创建", description = "新增报表结果字段")
    @PostMapping(Route.DEFINITION_RESULT_CREATE)
    @Transactional(rollbackFor = Exception.class)
    public R<ReportDefinitionResultResp> resultCreate(@Valid @RequestBody ReportDefinitionResultCreateReq req) {
        ReportDefinitionResult entity = BeanUtil.cp(req, ReportDefinitionResult.class);
        entity = reportDefinitionResultService.resultCreate(entity);
        ReportDefinitionResultResp resp = BeanUtil.cp(entity, ReportDefinitionResultResp.class);
        return R.ok(resp);
    }

    @Operation(summary = "3.报表结果字段-修改", description = "修改报表结果字段")
    @PostMapping(Route.DEFINITION_RESULT_UPDATE)
    @Transactional(rollbackFor = Exception.class)
    public R<ReportDefinitionResultResp> resultUpdate(@Valid @RequestBody ReportDefinitionResultUpdateReq req) {
        ReportDefinitionResult entity = BeanUtil.cp(req, ReportDefinitionResult.class);
        entity = reportDefinitionResultService.resultUpdate(entity);
        ReportDefinitionResultResp resp = BeanUtil.cp(entity, ReportDefinitionResultResp.class);
        return R.ok(resp);
    }

    @Operation(summary = "4.报表结果字段-删除", description = "删除报表结果字段")
    @PostMapping(Route.DEFINITION_RESULT_REMOVE)
    @Transactional(rollbackFor = Exception.class)
    public R<Integer> resultRemove(@Valid @RequestBody RemoveReq req) {
        ReportDefinitionResult entity = new ReportDefinitionResult();
        entity.setId(req.getId());
        Integer result = reportDefinitionResultService.resultRemove(entity);
        return R.ok(result);
    }

    @Operation(summary = "5.报表结果字段-自动提取", description = "从SQL脚本自动提取结果字段")
    @PostMapping(Route.DEFINITION_RESULT_EXTRACT)
    public R<List<ReportDefinitionResultResp>> resultExtract(@Valid @RequestBody ReportDefinitionResultExtractReq req) {
        ReportDefinitionResultDto dto = BeanUtil.cp(req, ReportDefinitionResultDto.class);
        List<ReportDefinitionResult> list = reportDefinitionResultService.resultsExtract(dto);
        List<ReportDefinitionResultResp> respList = BeanUtil.cp(list, ReportDefinitionResultResp.class);
        return R.ok(respList);
    }

}
