package com.wkclz.micro.report.rest;

import com.wkclz.core.base.PageData;
import com.wkclz.core.base.R;
import com.wkclz.micro.report.bean.entity.ReportDefinitionHis;
import com.wkclz.micro.report.bean.req.ReportDefinitionHisInfoReq;
import com.wkclz.micro.report.bean.req.ReportDefinitionHisPageReq;
import com.wkclz.micro.report.bean.resp.ReportDefinitionHisPageResp;
import com.wkclz.micro.report.bean.resp.ReportDefinitionHisResp;
import com.wkclz.micro.report.service.ReportDefinitionHisService;
import com.wkclz.tool.utils.BeanUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Tag(name = "2.报表定义历史", description = "报表定义历史版本管理接口")
@Validated
@RestController
@RequestMapping(Route.PREFIX)
public class ReportDefinitionHisRest {

    @Autowired
    private ReportDefinitionHisService reportDefinitionHisService;

    @Operation(summary = "1.报表定义历史-分页查询", description = "根据条件分页查询报表定义历史列表")
    @GetMapping(Route.DEFINITION_HIS_PAGE)
    public R<PageData<ReportDefinitionHisPageResp>> hisPage(@Valid ReportDefinitionHisPageReq req) {
        ReportDefinitionHis entity = BeanUtil.cp(req, ReportDefinitionHis.class);
        PageData<ReportDefinitionHis> page = reportDefinitionHisService.getHisPage(entity);
        PageData<ReportDefinitionHisPageResp> newPage = page.convert(ReportDefinitionHisPageResp.class);
        return R.ok(newPage);
    }

    @Operation(summary = "2.报表定义历史-详情", description = "根据ID查询报表定义历史详情")
    @GetMapping(Route.DEFINITION_HIS_DETAIL)
    public R<ReportDefinitionHisResp> hisDetail(@Valid ReportDefinitionHisInfoReq req) {
        ReportDefinitionHis entity = reportDefinitionHisService.getHisDetail(req.getId());
        if (entity == null) {
            return R.error("数据不存在");
        }
        ReportDefinitionHisResp resp = BeanUtil.cp(entity, ReportDefinitionHisResp.class);
        return R.ok(resp);
    }

}
