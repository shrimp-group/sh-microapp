package com.wkclz.auto.rest;

import com.wkclz.core.annotation.Router;

@Router(module = "micro-autotest", prefix = Route.PREFIX)
public interface Route {

    String PREFIX = "/micro-autotest";

    String API_LIST = "/api/list";
    String RUN = "/run";
    String REPORT = "/report";
    String REPORT_MD = "/report/md";
    String REPORT_HTML = "/report/html";
}
