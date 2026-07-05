package com.wkclz.micro.report.rest;

import com.wkclz.core.base.R;
import com.wkclz.micro.report.bean.dto.ReportDefinitionDto;
import com.wkclz.micro.report.bean.req.ReportExecExportReq;
import com.wkclz.micro.report.bean.req.ReportExecInfoReq;
import com.wkclz.micro.report.bean.req.ReportExecQueryReq;
import com.wkclz.micro.report.bean.resp.ReportDefinitionParamResp;
import com.wkclz.micro.report.bean.resp.ReportDefinitionResultResp;
import com.wkclz.micro.report.bean.resp.ReportExecInfoResp;
import com.wkclz.micro.report.bean.resp.ReportExecOptionsResp;
import com.wkclz.micro.report.helper.ReportExportHelper;
import com.wkclz.micro.report.service.ReportExecService;
import com.wkclz.tool.utils.BeanUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Tag(name = "5.报表执行", description = "报表执行与导出接口")
@Validated
@RestController
@RequestMapping(Route.PREFIX)
public class ReportExecRest {

    @Autowired
    private ReportExecService reportExecService;
    @Autowired
    private ReportExportHelper reportExportHelper;

    @Operation(summary = "1.报表执行-选项列表", description = "获取所有可用报表的选项列表")
    @GetMapping(Route.EXEC_OPTIONS)
    public R<List<ReportExecOptionsResp>> execOptions() {
        List<ReportDefinitionDto> options = reportExecService.getReportOptions();
        List<ReportExecOptionsResp> respList = BeanUtil.cp(options, ReportExecOptionsResp.class);
        return R.ok(respList);
    }

    @Operation(summary = "2.报表执行-详情", description = "根据报表编码获取报表详情（含参数和结果元数据）")
    @GetMapping(Route.EXEC_INFO)
    public R<ReportExecInfoResp> execInfo(@Valid ReportExecInfoReq req) {
        ReportDefinitionDto info = reportExecService.getReportInfo(req.getReportCode());
        ReportExecInfoResp resp = BeanUtil.cp(info, ReportExecInfoResp.class);
        if (info.getParams() != null) {
            List<ReportDefinitionParamResp> paramResps = BeanUtil.cp(info.getParams(), ReportDefinitionParamResp.class);
            resp.setParams(paramResps);
        }
        if (info.getResults() != null) {
            List<ReportDefinitionResultResp> resultResps = BeanUtil.cp(info.getResults(), ReportDefinitionResultResp.class);
            resp.setResults(resultResps);
        }
        return R.ok(resp);
    }

    @Operation(summary = "3.报表执行-查询", description = "执行报表查询")
    @GetMapping(Route.EXEC_QUERY)
    public R<Object> execQuery(String reportCode,
                               @RequestParam(required = false) Integer current,
                               @RequestParam(required = false) Integer size,
                               @RequestParam(required = false) Map<String, Object> params) {
        // 过滤掉分页参数
        if (params != null) {
            params.remove("reportCode");
            params.remove("current");
            params.remove("size");
        }

        Object result = reportExecService.execQuery(reportCode, params, current, size);
        return R.ok(result);
    }

    @Operation(summary = "4.报表执行-导出Excel", description = "导出报表数据为Excel")
    @PostMapping(Route.EXEC_EXPORT)
    public void execExport(@RequestBody Map<String, Object> requestBody, HttpServletResponse response) {

        String reportCode = (String) requestBody.get("reportCode");

        List<LinkedHashMap<String, Object>> data = reportExecService.getReportData(reportCode, requestBody);
        LinkedHashMap<String, String> headers = reportExecService.getExportHeaders(reportCode);
        String fileName = reportCode != null ? reportCode : "report";

        reportExportHelper.export(response, data, headers, fileName);
    }

}
