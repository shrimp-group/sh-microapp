package com.wkclz.micro.rmcheck.rest;


import com.wkclz.core.annotation.Router;

/**
 * Description:
 * @author wangkaicun @ 2017-10-19 上午12:46
 */

@Router(module = "micro-rmcheck", prefix = Route.PREFIX)
public interface Route {

    String PREFIX = "/micro-rmcheck";


    String RM_CHECK_RULE_PAGE = "/rule/page";
    String RM_CHECK_RULE_INFO = "/rule/info";
    String RM_CHECK_RULE_CREATE = "/rule/create";
    String RM_CHECK_RULE_UPDATE = "/rule/update";
    String RM_CHECK_RULE_REMOVE = "/rule/remove";

    String RM_CHECK_RULE_ITEM_LIST = "/rule/item/list";
    String RM_CHECK_RULE_ITEM_INFO = "/rule/item/info";
    String RM_CHECK_RULE_ITEM_CREATE = "/rule/item/create";
    String RM_CHECK_RULE_ITEM_UPDATE = "/rule/item/update";
    String RM_CHECK_RULE_ITEM_REMOVE = "/rule/item/remove";


}
