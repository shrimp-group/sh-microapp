package com.wkclz.micro.dag.rest;

import com.wkclz.core.annotation.Router;

/**
 * micro-dag 路由常量
 */
@Router(module = "micro-dag", prefix = Route.PREFIX)
public interface Route {

    String PREFIX = "/micro-dag";

    // 定义
    String DEFINITION_SAVE = "/definition/save";
    String DEFINITION_PUBLISH = "/definition/publish";
    String DEFINITION_PAGE = "/definition/page";
    String DEFINITION_DETAIL = "/definition/detail";
    String DEFINITION_COPY = "/definition/copy";

    // 元能力
    String HANDLER_LIST = "/handler/list";
    String EXPRESSION_PREVIEW = "/expression/preview";

    // 实例
    String INSTANCE_LAUNCH = "/instance/launch";
    String INSTANCE_PAGE = "/instance/page";
    String INSTANCE_DETAIL = "/instance/detail";
    String INSTANCE_TERMINATE = "/instance/terminate";
    String INSTANCE_RERUN_FROM = "/instance/rerun-from";

    // 节点
    String NODE_DETAIL = "/node/detail";
    String NODE_INTERRUPT = "/node/interrupt";
    String NODE_PARAMS = "/node/params";
    String NODE_SKIP = "/node/skip";
    String NODE_RESET = "/node/reset";
    String NODE_RERUN = "/node/rerun";
}
