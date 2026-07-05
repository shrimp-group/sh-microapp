package com.wkclz.micro.audit.rest;


import com.wkclz.core.annotation.ApiDesc;
import com.wkclz.core.annotation.Router;

/**
 * Description:
 * Created: wangkaicun @ 2017-10-19 上午12:46
 */
@Router(module = "micro-audit", prefix = Route.PREFIX)
public interface Route {


    String PREFIX = "/micro-audit";

    /**
     * 变更记录配置
     */

    @ApiDesc("1. 变更记录-分页")
    String CHANGE_LOG_PAGE = "/change/log/page";
    @ApiDesc("2. 变更记录-详情")
    String CHANGE_LOG_INFO = "/change/log/info";

}
