package com.wkclz.micro.flowable.rest;

import com.wkclz.core.annotation.Router;

/**
 * micro-flowable 路由常量。
 *
 * <p>对接 sh-flowable-server，封装流程定义 / 流程实例 / 任务 / 历史等能力。
 * 具体端点待内部功能规划后补充。
 */
@Router(module = "micro-flowable", prefix = Route.PREFIX)
public interface Route {

    String PREFIX = "/micro-flowable";

    // TODO 内部功能规划后补充端点常量，例如：
    // String PROCESS_DEF_PAGE = "/processDef/page";
}
