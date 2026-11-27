package com.wkclz.micro.report.rest;

import com.wkclz.core.base.R;
import com.wkclz.micro.report.bean.dto.ReportDefinitionDto;
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
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Tag(name = "5.报表执行", description = "报表执行与导出接口")
@Slf4j
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
        log.info("查询报表选项列表");
        List<ReportDefinitionDto> options = reportExecService.getReportOptions();
        List<ReportExecOptionsResp> respList = BeanUtil.cp(options, ReportExecOptionsResp.class);
        return R.ok(respList);
    }

    @Operation(summary = "2.报表执行-详情", description = "根据报表编码获取报表详情（含参数和结果元数据）")
    @GetMapping(Route.EXEC_INFO)
    public R<ReportExecInfoResp> execInfo(@Valid ReportExecInfoReq req) {
        log.info("报表执行详情查询, req={}", req);
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

    /**
     * 执行报表查询（GET 参数按字段名绑定到 ReportExecQueryReq，URL 与参数名保持兼容）。
     * <p>返回数据结构由报表定义的 ReturnType（OBJECT/LIST/PAGE）在运行时决定，
     * 静态类型无法无损明确为 Resp，故保留 R<Object>。</p>
     */
    @Operation(summary = "3.报表执行-查询", description = "执行报表查询")
    @GetMapping(Route.EXEC_QUERY)
    public R<Object> execQuery(@Valid ReportExecQueryReq req, HttpServletRequest request) {
        log.info("报表查询, req={}", req);

        // 兼容原 @RequestParam Map 行为：收集除 req 已绑定字段外的所有 query 参数作为动态查询参数（多值参数取数组）
        Map<String, Object> params = req.getParams() != null ? req.getParams() : new HashMap<>();
        request.getParameterMap().forEach((key, values) -> {
            if (!key.startsWith("params[")) {
                params.put(key, values.length == 1 ? values[0] : values);
            }
        });
        // 过滤掉分页参数
        params.remove("reportCode");
        params.remove("current");
        params.remove("size");

        Object result = reportExecService.execQuery(req.getReportCode(), params, req.getCurrent(), req.getSize());
        return R.ok(result);
    }

    @Operation(summary = "4.报表执行-导出Excel", description = "导出报表数据为Excel")
    @PostMapping(Route.EXEC_EXPORT)
    public void execExport(@RequestBody Map<String, Object> requestBody, HttpServletResponse response) {
        log.info("报表导出Excel, req={}", requestBody);
        String reportCode = (String) requestBody.get("reportCode");

        List<LinkedHashMap<String, Object>> data = reportExecService.getReportData(reportCode, requestBody);
        LinkedHashMap<String, String> headers = reportExecService.getExportHeaders(reportCode);
        String fileName = reportCode != null ? reportCode : "report";

        reportExportHelper.export(response, data, headers, fileName);
    }

}
