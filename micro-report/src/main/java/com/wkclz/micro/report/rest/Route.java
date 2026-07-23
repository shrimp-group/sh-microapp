package com.wkclz.micro.report.rest;

import com.wkclz.core.annotation.Router;

@Router(module = "micro-report", prefix = Route.PREFIX)
public interface Route {

    String PREFIX = "/micro-report";

    // 报表定义管理
    String DEFINITION_PAGE = "/definition/page";
    String DEFINITION_DETAIL = "/definition/detail";
    String DEFINITION_CREATE = "/definition/create";
    String DEFINITION_UPDATE = "/definition/update";
    String DEFINITION_REMOVE = "/definition/remove";
    String DEFINITION_TEST = "/definition/test";

    // 报表定义历史版本
    String DEFINITION_HIS_PAGE = "/definition/his/page";
    String DEFINITION_HIS_DETAIL = "/definition/his/detail";

    // 报表参数管理
    String DEFINITION_PARAM_LIST = "/definition/param/list";
    String DEFINITION_PARAM_CREATE = "/definition/param/create";
    String DEFINITION_PARAM_UPDATE = "/definition/param/update";
    String DEFINITION_PARAM_REMOVE = "/definition/param/remove";
    String DEFINITION_PARAM_EXTRACT = "/definition/param/extract";

    // 报表结果字段管理
    String DEFINITION_RESULT_LIST = "/definition/result/list";
    String DEFINITION_RESULT_CREATE = "/definition/result/create";
    String DEFINITION_RESULT_UPDATE = "/definition/result/update";
    String DEFINITION_RESULT_REMOVE = "/definition/result/remove";
    String DEFINITION_RESULT_EXTRACT = "/definition/result/extract";

    // 报表执行
    String EXEC_OPTIONS = "/exec/options";
    String EXEC_INFO = "/exec/info";
    String EXEC_QUERY = "/exec/query";
    String EXEC_EXPORT = "/exec/export";

}
