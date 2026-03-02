package com.wkclz.micro.rmcheck.rest;


import com.wkclz.core.annotation.Desc;
import com.wkclz.core.annotation.Router;

/**
 * Description:
 * @author wangkaicun @ 2017-10-19 上午12:46
 */

@Router(module = "micro-rmcheck", prefix = Route.PREFIX)
public interface Route {

    String PREFIX = "/micro-rmcheck";


    @Desc("1. 删除检查规则-分页")
    String RM_CHECK_RULE_PAGE = "/rule/page";
    @Desc("2. 删除检查规则-详情")
    String RM_CHECK_RULE_INFO = "/rule/info";
    @Desc("3. 删除检查规则-新增")
    String RM_CHECK_RULE_CREATE = "/rule/create";
    @Desc("4. 删除检查规则-更新")
    String RM_CHECK_RULE_UPDATE = "/rule/update";
    @Desc("5. 删除检查规则-移除")
    String RM_CHECK_RULE_REMOVE = "/rule/remove";

    @Desc("1. 删除检查规则-检查项-列表")
    String RM_CHECK_RULE_ITEM_LIST = "/rule/item/list";
    @Desc("2. 删除检查规则-检查项-详情")
    String RM_CHECK_RULE_ITEM_INFO = "/rule/item/info";
    @Desc("3. 删除检查规则-检查项-新增")
    String RM_CHECK_RULE_ITEM_CREATE = "/rule/item/create";
    @Desc("4. 删除检查规则-检查项-更新")
    String RM_CHECK_RULE_ITEM_UPDATE = "/rule/item/update";
    @Desc("5. 删除检查规则-检查项-移除")
    String RM_CHECK_RULE_ITEM_REMOVE = "/rule/item/remove";


}
