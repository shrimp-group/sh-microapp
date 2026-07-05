package com.wkclz.micro.report.rest;

import com.wkclz.core.annotation.ApiDesc;
import com.wkclz.core.annotation.Router;

@Router(module = "micro-report", prefix = Route.PREFIX)
public interface Route {

    String PREFIX = "/micro-report";

    // 报表定义管理
    @ApiDesc("1. 报表定义分页查询")
    String DEFINITION_PAGE = "/definition/page";
    @ApiDesc("2. 报表定义详情")
    String DEFINITION_DETAIL = "/definition/detail";
    @ApiDesc("3. 报表定义新增")
    String DEFINITION_CREATE = "/definition/create";
    @ApiDesc("4. 报表定义修改")
    String DEFINITION_UPDATE = "/definition/update";
    @ApiDesc("5. 报表定义删除")
    String DEFINITION_REMOVE = "/definition/remove";
    @ApiDesc("6. SQL测试")
    String DEFINITION_TEST = "/definition/test";

    // 报表定义历史版本
    @ApiDesc("1. 报表定义历史分页")
    String DEFINITION_HIS_PAGE = "/definition/his/page";
    @ApiDesc("2. 报表定义历史详情")
    String DEFINITION_HIS_DETAIL = "/definition/his/detail";

    // 报表参数管理
    @ApiDesc("1. 报表参数列表")
    String DEFINITION_PARAM_LIST = "/definition/param/list";
    @ApiDesc("2. 报表参数新增")
    String DEFINITION_PARAM_CREATE = "/definition/param/create";
    @ApiDesc("3. 报表参数修改")
    String DEFINITION_PARAM_UPDATE = "/definition/param/update";
    @ApiDesc("4. 报表参数删除")
    String DEFINITION_PARAM_REMOVE = "/definition/param/remove";
    @ApiDesc("5. 报表参数自动提取")
    String DEFINITION_PARAM_EXTRACT = "/definition/param/extract";

    // 报表结果字段管理
    @ApiDesc("1. 报表结果字段列表")
    String DEFINITION_RESULT_LIST = "/definition/result/list";
    @ApiDesc("2. 报表结果字段新增")
    String DEFINITION_RESULT_CREATE = "/definition/result/create";
    @ApiDesc("3. 报表结果字段修改")
    String DEFINITION_RESULT_UPDATE = "/definition/result/update";
    @ApiDesc("4. 报表结果字段删除")
    String DEFINITION_RESULT_REMOVE = "/definition/result/remove";
    @ApiDesc("5. 报表结果字段自动提取")
    String DEFINITION_RESULT_EXTRACT = "/definition/result/extract";

    // 报表执行
    @ApiDesc("1. 报表选项列表")
    String EXEC_OPTIONS = "/exec/options";
    @ApiDesc("2. 报表详情（含参数和结果元数据）")
    String EXEC_INFO = "/exec/info";
    @ApiDesc("3. 执行报表查询")
    String EXEC_QUERY = "/exec/query";
    @ApiDesc("4. 导出Excel")
    String EXEC_EXPORT = "/exec/export";

}
