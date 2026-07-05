package com.wkclz.micro.rmcheck.rest;


import com.wkclz.core.annotation.ApiDesc;
import com.wkclz.core.annotation.Router;

/**
 * Description:
 * @author wangkaicun @ 2017-10-19 上午12:46
 */

@Router(module = "micro-rmcheck", prefix = Route.PREFIX)
public interface Route {

    String PREFIX = "/micro-rmcheck";


    @ApiDesc("1. 删除检查规则-分页")
    String RM_CHECK_RULE_PAGE = "/rule/page";
    @ApiDesc("2. 删除检查规则-详情")
    String RM_CHECK_RULE_INFO = "/rule/info";
    @ApiDesc("3. 删除检查规则-新增")
    String RM_CHECK_RULE_CREATE = "/rule/create";
    @ApiDesc("4. 删除检查规则-更新")
    String RM_CHECK_RULE_UPDATE = "/rule/update";
    @ApiDesc("5. 删除检查规则-移除")
    String RM_CHECK_RULE_REMOVE = "/rule/remove";

    @ApiDesc("1. 删除检查规则-检查项-列表")
    String RM_CHECK_RULE_ITEM_LIST = "/rule/item/list";
    @ApiDesc("2. 删除检查规则-检查项-详情")
    String RM_CHECK_RULE_ITEM_INFO = "/rule/item/info";
    @ApiDesc("3. 删除检查规则-检查项-新增")
    String RM_CHECK_RULE_ITEM_CREATE = "/rule/item/create";
    @ApiDesc("4. 删除检查规则-检查项-更新")
    String RM_CHECK_RULE_ITEM_UPDATE = "/rule/item/update";
    @ApiDesc("5. 删除检查规则-检查项-移除")
    String RM_CHECK_RULE_ITEM_REMOVE = "/rule/item/remove";


}
