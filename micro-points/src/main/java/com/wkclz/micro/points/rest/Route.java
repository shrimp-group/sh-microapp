package com.wkclz.micro.points.rest;

import com.wkclz.core.annotation.Router;

/**
 * 积分模块路由常量
 */
@Router(module = "micro-points", prefix = Route.PREFIX)
public interface Route {

    String PREFIX = "/micro-points";

    /**
     * C 端接口前缀（与 micro-pay 的 CustomPayOrderRest 约定对齐）
     * 完整 C 端路径：/micro-points/custom/*
     */

    /**
     * C 端接口（基于登录 userCode，只读）
     */

    String CUSTOM_WALLET = "/custom/wallet";
    String CUSTOM_EARN_PAGE = "/custom/earn/page";
    String CUSTOM_CONSUME_PAGE = "/custom/consume/page";
    String CUSTOM_TRIAL = "/custom/trial";

    /**
     * 运营端接口（按 userCode 查询/操作）
     */

    String ADMIN_ISSUE = "/admin/issue";
    String ADMIN_WALLET = "/admin/wallet";
    String ADMIN_EARN_PAGE = "/admin/earn/page";
    String ADMIN_CONSUME_PAGE = "/admin/consume/page";
    String ADMIN_CONSUME_DEDUCTION_PAGE = "/admin/consume/deduction/page";
    String ADMIN_RECONCILE = "/admin/reconcile";

}
