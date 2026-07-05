package com.wkclz.auto.rest;

import com.wkclz.auto.bean.ApiInfo;
import com.wkclz.auto.bean.TestReport;
import com.wkclz.auto.bean.req.ApiListReq;
import com.wkclz.auto.bean.req.RunReq;
import com.wkclz.auto.executor.TestExecutor;
import com.wkclz.auto.report.ReportGenerator;
import com.wkclz.auto.scanner.ApiScanner;
import com.wkclz.core.base.R;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "自动化测试", description = "自动化测试管理")
@Validated
@RestController
@RequestMapping(Route.PREFIX)
public class AutoTestRest {

    @Autowired
    private ApiScanner apiScanner;

    @Autowired
    private TestExecutor testExecutor;

    @Autowired
    private ReportGenerator reportGenerator;

    private volatile TestReport latestReport;

    @Operation(summary = "1. 接口列表")
    @GetMapping(Route.API_LIST)
    public R<List<ApiInfo>> apiList(@Valid ApiListReq req) {
        List<ApiInfo> apiInfos = apiScanner.scan(req.getPackagePath());
        return R.ok(apiInfos);
    }

    @Operation(summary = "2. 执行测试")
    @PostMapping(Route.RUN)
    public R<TestReport> run(@Valid @RequestBody RunReq req) {
        TestReport report = testExecutor.execute(req.getPackagePath());
        latestReport = report;

        if (req.getReportDir() != null) {
            reportGenerator.saveReport(report, req.getReportDir());
        }

        return R.ok(report);
    }

    @Operation(summary = "3. 测试报告")
    @GetMapping(Route.REPORT)
    public R<TestReport> report() {
        if (latestReport == null) {
            return R.error("no test report available, please run tests first");
        }
        return R.ok(latestReport);
    }

    @Operation(summary = "4. 测试报告(MD)")
    @GetMapping(value = Route.REPORT_MD, produces = MediaType.TEXT_MARKDOWN_VALUE)
    @ResponseBody
    public String reportMd() {
        if (latestReport == null) {
            return "# No Report\n\nPlease run tests first.";
        }
        return reportGenerator.generateMd(latestReport);
    }

    @Operation(summary = "5. 测试报告(HTML)")
    @GetMapping(value = Route.REPORT_HTML, produces = MediaType.TEXT_HTML_VALUE)
    @ResponseBody
    public String reportHtml() {
        if (latestReport == null) {
            return "<html><body><h1>No Report</h1><p>Please run tests first.</p></body></html>";
        }
        return reportGenerator.generateHtml(latestReport);
    }
}
