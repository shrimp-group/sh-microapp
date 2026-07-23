package com.wkclz.micro.mask.rest;


import com.wkclz.core.annotation.Router;

/**
 * Description:
 * Created: wangkaicun @ 2017-10-19 上午12:46
 */
@Router(module = "micro-mask", prefix = Route.PREFIX)
public interface Route {

    String PREFIX = "/micro-mask";



    /**
     * 脱敏规则配置
     */

    String RULE_PAGE = "/rule/page";
    String RULE_INFO = "/rule/info";
    String RULE_CREATE = "/rule/create";
    String RULE_UPDATE = "/rule/update";
    String RULE_REMOVE = "/rule/remove";


    String RULE_TEST = "/rule/test";
    String RULE_VERIFY = "/rule/verify";

}
