package com.wkclz.auto.report;

import com.wkclz.auto.bean.TestCaseResult;
import com.wkclz.auto.bean.TestReport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Component
public class ReportGenerator {

    private static final Logger logger = LoggerFactory.getLogger(ReportGenerator.class);
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public String generateMd(TestReport report) {
        StringBuilder sb = new StringBuilder();

        sb.append("# AutoTest Report\n\n");

        sb.append("## Summary\n\n");
        sb.append("| Metric | Value |\n");
        sb.append("|--------|-------|\n");
        sb.append("| Start Time | ").append(report.getStartTime() != null ? report.getStartTime().format(FORMATTER) : "-").append(" |\n");
        sb.append("| End Time | ").append(report.getEndTime() != null ? report.getEndTime().format(FORMATTER) : "-").append(" |\n");
        sb.append("| Total Time | ").append(report.getTotalCostTimeMs()).append(" ms |\n");
        sb.append("| Total APIs | ").append(report.getTotalApiCount()).append(" |\n");
        sb.append("| Success | ").append(report.getSuccessCount()).append(" |\n");
        sb.append("| Fail | ").append(report.getFailCount()).append(" |\n");
        sb.append("| Error | ").append(report.getErrorCount()).append(" |\n");
        double successRate = report.getTotalApiCount() > 0 ? (double) report.getSuccessCount() / report.getTotalApiCount() * 100 : 0;
        sb.append("| Success Rate | ").append(String.format("%.1f%%", successRate)).append(" |\n\n");

        sb.append("## Test Details\n\n");
        sb.append("| # | Method | URI | Description | Status | HTTP Code | Time(ms) | Error |\n");
        sb.append("|---|--------|-----|-------------|--------|-----------|----------|-------|\n");

        List<TestCaseResult> results = report.getResults();
        if (results != null) {
            for (int i = 0; i < results.size(); i++) {
                TestCaseResult r = results.get(i);
                sb.append("| ").append(i + 1).append(" ");
                sb.append("| ").append(r.getMethod()).append(" ");
                sb.append("| ").append(r.getUri()).append(" ");
                sb.append("| ").append(r.getDesc() != null ? r.getDesc() : "-").append(" ");
                sb.append("| ").append(r.isSuccess() ? "PASS" : "FAIL").append(" ");
                sb.append("| ").append(r.getHttpStatus()).append(" ");
                sb.append("| ").append(r.getCostTimeMs()).append(" ");
                sb.append("| ").append(r.getErrorMessage() != null ? r.getErrorMessage() : "-").append(" |\n");
            }
        }

        List<TestCaseResult> failedResults = results != null ? results.stream().filter(r -> !r.isSuccess()).toList() : List.of();
        if (!failedResults.isEmpty()) {
            sb.append("\n## Failed Cases Detail\n\n");
            for (TestCaseResult r : failedResults) {
                sb.append("### ").append(r.getMethod()).append(" ").append(r.getUri()).append("\n\n");
                if (r.getDesc() != null) {
                    sb.append("- **Description**: ").append(r.getDesc()).append("\n");
                }
                sb.append("- **HTTP Status**: ").append(r.getHttpStatus()).append("\n");
                sb.append("- **Cost Time**: ").append(r.getCostTimeMs()).append(" ms\n");
                if (r.getRequestBody() != null) {
                    sb.append("- **Request Body**: ").append(r.getRequestBody()).append("\n");
                }
                if (r.getErrorMessage() != null) {
                    sb.append("- **Error**: ").append(r.getErrorMessage()).append("\n");
                }
                if (r.getResponseBody() != null) {
                    sb.append("- **Response Body**: ").append(r.getResponseBody()).append("\n");
                }
                sb.append("\n");
            }
        }

        return sb.toString();
    }

    public String generateHtml(TestReport report) {
        StringBuilder sb = new StringBuilder();

        sb.append("<!DOCTYPE html>\n");
        sb.append("<html lang=\"zh-CN\">\n<head>\n");
        sb.append("<meta charset=\"UTF-8\">\n");
        sb.append("<meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n");
        sb.append("<title>AutoTest Report</title>\n");
        sb.append("<style>\n");
        sb.append("body { font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif; margin: 0; padding: 20px; background: #f5f5f5; color: #333; }\n");
        sb.append(".container { max-width: 1200px; margin: 0 auto; }\n");
        sb.append("h1 { color: #1a1a1a; border-bottom: 2px solid #4CAF50; padding-bottom: 10px; }\n");
        sb.append("h2 { color: #2c3e50; margin-top: 30px; }\n");
        sb.append(".summary { display: grid; grid-template-columns: repeat(auto-fit, minmax(150px, 1fr)); gap: 15px; margin: 20px 0; }\n");
        sb.append(".summary-card { background: white; border-radius: 8px; padding: 15px; box-shadow: 0 2px 4px rgba(0,0,0,0.1); text-align: center; }\n");
        sb.append(".summary-card .label { font-size: 12px; color: #888; text-transform: uppercase; }\n");
        sb.append(".summary-card .value { font-size: 24px; font-weight: bold; margin-top: 5px; }\n");
        sb.append(".summary-card.success .value { color: #4CAF50; }\n");
        sb.append(".summary-card.fail .value { color: #f44336; }\n");
        sb.append(".summary-card.error .value { color: #FF9800; }\n");
        sb.append(".summary-card.total .value { color: #2196F3; }\n");
        sb.append(".summary-card.rate .value { color: #9C27B0; }\n");
        sb.append("table { width: 100%; border-collapse: collapse; background: white; border-radius: 8px; overflow: hidden; box-shadow: 0 2px 4px rgba(0,0,0,0.1); }\n");
        sb.append("th { background: #2c3e50; color: white; padding: 12px 8px; text-align: left; font-size: 13px; }\n");
        sb.append("td { padding: 10px 8px; border-bottom: 1px solid #eee; font-size: 13px; }\n");
        sb.append("tr:hover { background: #f8f9fa; }\n");
        sb.append(".badge { padding: 3px 8px; border-radius: 4px; font-size: 11px; font-weight: bold; }\n");
        sb.append(".badge.pass { background: #E8F5E9; color: #2E7D32; }\n");
        sb.append(".badge.fail { background: #FFEBEE; color: #C62828; }\n");
        sb.append(".method { padding: 2px 6px; border-radius: 3px; font-size: 11px; font-weight: bold; }\n");
        sb.append(".method.GET { background: #E3F2FD; color: #1565C0; }\n");
        sb.append(".method.POST { background: #FFF3E0; color: #E65100; }\n");
        sb.append(".method.PUT { background: #F3E5F5; color: #6A1B9A; }\n");
        sb.append(".method.DELETE { background: #FCE4EC; color: #AD1457; }\n");
        sb.append(".detail-section { margin-top: 30px; }\n");
        sb.append(".detail-card { background: white; border-radius: 8px; padding: 15px; margin: 10px 0; box-shadow: 0 2px 4px rgba(0,0,0,0.1); border-left: 4px solid #f44336; }\n");
        sb.append(".detail-card h3 { margin: 0 0 10px 0; color: #C62828; font-size: 14px; }\n");
        sb.append(".detail-card p { margin: 5px 0; font-size: 13px; }\n");
        sb.append(".detail-card code { background: #f5f5f5; padding: 2px 4px; border-radius: 3px; font-size: 12px; }\n");
        sb.append("</style>\n");
        sb.append("</head>\n<body>\n");
        sb.append("<div class=\"container\">\n");

        sb.append("<h1>AutoTest Report</h1>\n");

        double successRate = report.getTotalApiCount() > 0 ? (double) report.getSuccessCount() / report.getTotalApiCount() * 100 : 0;

        sb.append("<div class=\"summary\">\n");
        sb.append("<div class=\"summary-card total\"><div class=\"label\">Total APIs</div><div class=\"value\">").append(report.getTotalApiCount()).append("</div></div>\n");
        sb.append("<div class=\"summary-card success\"><div class=\"label\">Success</div><div class=\"value\">").append(report.getSuccessCount()).append("</div></div>\n");
        sb.append("<div class=\"summary-card fail\"><div class=\"label\">Fail</div><div class=\"value\">").append(report.getFailCount()).append("</div></div>\n");
        sb.append("<div class=\"summary-card error\"><div class=\"label\">Error</div><div class=\"value\">").append(report.getErrorCount()).append("</div></div>\n");
        sb.append("<div class=\"summary-card rate\"><div class=\"label\">Success Rate</div><div class=\"value\">").append(String.format("%.1f%%", successRate)).append("</div></div>\n");
        sb.append("<div class=\"summary-card total\"><div class=\"label\">Total Time</div><div class=\"value\">").append(report.getTotalCostTimeMs()).append("ms</div></div>\n");
        sb.append("</div>\n");

        sb.append("<h2>Test Details</h2>\n");
        sb.append("<table>\n");
        sb.append("<thead><tr><th>#</th><th>Method</th><th>URI</th><th>Description</th><th>Status</th><th>HTTP Code</th><th>Time(ms)</th></tr></thead>\n");
        sb.append("<tbody>\n");

        List<TestCaseResult> results = report.getResults();
        if (results != null) {
            for (int i = 0; i < results.size(); i++) {
                TestCaseResult r = results.get(i);
                sb.append("<tr>");
                sb.append("<td>").append(i + 1).append("</td>");
                sb.append("<td><span class=\"method ").append(r.getMethod()).append("\">").append(r.getMethod()).append("</span></td>");
                sb.append("<td>").append(r.getUri()).append("</td>");
                sb.append("<td>").append(r.getDesc() != null ? r.getDesc() : "-").append("</td>");
                sb.append("<td><span class=\"badge ").append(r.isSuccess() ? "pass" : "fail").append("\">").append(r.isSuccess() ? "PASS" : "FAIL").append("</span></td>");
                sb.append("<td>").append(r.getHttpStatus()).append("</td>");
                sb.append("<td>").append(r.getCostTimeMs()).append("</td>");
                sb.append("</tr>\n");
            }
        }

        sb.append("</tbody></table>\n");

        List<TestCaseResult> failedResults = results != null ? results.stream().filter(r -> !r.isSuccess()).toList() : List.of();
        if (!failedResults.isEmpty()) {
            sb.append("<div class=\"detail-section\">\n");
            sb.append("<h2>Failed Cases Detail</h2>\n");
            for (TestCaseResult r : failedResults) {
                sb.append("<div class=\"detail-card\">\n");
                sb.append("<h3>").append(r.getMethod()).append(" ").append(r.getUri()).append("</h3>\n");
                if (r.getDesc() != null) {
                    sb.append("<p><strong>Description:</strong> ").append(r.getDesc()).append("</p>\n");
                }
                sb.append("<p><strong>HTTP Status:</strong> ").append(r.getHttpStatus()).append("</p>\n");
                sb.append("<p><strong>Cost Time:</strong> ").append(r.getCostTimeMs()).append(" ms</p>\n");
                if (r.getErrorMessage() != null) {
                    sb.append("<p><strong>Error:</strong> <code>").append(r.getErrorMessage()).append("</code></p>\n");
                }
                if (r.getResponseBody() != null) {
                    sb.append("<p><strong>Response:</strong> <code>").append(r.getResponseBody().length() > 500 ? r.getResponseBody().substring(0, 500) + "..." : r.getResponseBody()).append("</code></p>\n");
                }
                sb.append("</div>\n");
            }
            sb.append("</div>\n");
        }

        sb.append("</div>\n</body>\n</html>");

        return sb.toString();
    }

    public void saveReport(TestReport report, String dirPath) {
        try {
            Path dir = Paths.get(dirPath);
            Files.createDirectories(dir);

            String timestamp = report.getStartTime() != null ? report.getStartTime().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) : String.valueOf(System.currentTimeMillis());

            String mdContent = generateMd(report);
            Path mdPath = dir.resolve("autotest_report_" + timestamp + ".md");
            Files.writeString(mdPath, mdContent);
            logger.info("MD report saved: {}", mdPath.toAbsolutePath());

            String htmlContent = generateHtml(report);
            Path htmlPath = dir.resolve("autotest_report_" + timestamp + ".html");
            Files.writeString(htmlPath, htmlContent);
            logger.info("HTML report saved: {}", htmlPath.toAbsolutePath());

        } catch (IOException e) {
            logger.error("failed to save report: {}", e.getMessage(), e);
        }
    }
}
