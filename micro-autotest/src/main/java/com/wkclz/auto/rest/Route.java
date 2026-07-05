package com.wkclz.auto.rest;

import com.wkclz.core.annotation.ApiDesc;
import com.wkclz.core.annotation.Router;

@Router(module = "micro-autotest", prefix = Route.PREFIX)
public interface Route {

    String PREFIX = "/micro-autotest";

    @ApiDesc("1. 接口列表")
    String API_LIST = "/api/list";
    @ApiDesc("2. 执行测试")
    String RUN = "/run";
    @ApiDesc("3. 测试报告")
    String REPORT = "/report";
    @ApiDesc("4. 测试报告(MD)")
    String REPORT_MD = "/report/md";
    @ApiDesc("5. 测试报告(HTML)")
    String REPORT_HTML = "/report/html";
}
