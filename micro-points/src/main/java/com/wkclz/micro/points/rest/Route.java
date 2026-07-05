package com.wkclz.micro.points.rest;

import com.wkclz.core.annotation.ApiDesc;
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

    @ApiDesc("1. 钱包查询")
    String CUSTOM_WALLET = "/custom/wallet";
    @ApiDesc("2. 获取流水分页")
    String CUSTOM_EARN_PAGE = "/custom/earn/page";
    @ApiDesc("3. 消费流水分页")
    String CUSTOM_CONSUME_PAGE = "/custom/consume/page";
    @ApiDesc("4. 积分试算")
    String CUSTOM_TRIAL = "/custom/trial";

    /**
     * 运营端接口（按 userCode 查询/操作）
     */

    @ApiDesc("1. 管理员手动发放积分")
    String ADMIN_ISSUE = "/admin/issue";
    @ApiDesc("2. 用户钱包查询")
    String ADMIN_WALLET = "/admin/wallet";
    @ApiDesc("3. 获取流水分页")
    String ADMIN_EARN_PAGE = "/admin/earn/page";
    @ApiDesc("4. 消费流水分页")
    String ADMIN_CONSUME_PAGE = "/admin/consume/page";
    @ApiDesc("5. 消费扣减明细分页")
    String ADMIN_CONSUME_DEDUCTION_PAGE = "/admin/consume/deduction/page";
    @ApiDesc("6. 对账查询")
    String ADMIN_RECONCILE = "/admin/reconcile";

}
