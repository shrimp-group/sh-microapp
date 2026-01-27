package com.wkclz.micro.rmcheck.rest;


import com.wkclz.core.annotation.Desc;
import com.wkclz.core.annotation.Router;

/**
 * Description:
 * @author wangkaicun @ 2017-10-19 上午12:46
 */

@Router(module = "micro-rm", prefix = Route.PREFIX)
public interface Route {

    String PREFIX = "/micro-rm";


    @Desc("1. 删除检查规则-分页")
    String RM_CHECK_RULE_PAGE = "/rm/check/rule/page";
    @Desc("2. 删除检查规则-详情")
    String RM_CHECK_RULE_INFO = "/rm/check/rule/info";
    @Desc("3. 删除检查规则-新增")
    String RM_CHECK_RULE_CREATE = "/rm/check/rule/create";
    @Desc("4. 删除检查规则-更新")
    String RM_CHECK_RULE_UPDATE = "/rm/check/rule/update";
    @Desc("5. 删除检查规则-移除")
    String RM_CHECK_RULE_REMOVE = "/rm/check/rule/remove";

    @Desc("1. 删除检查规则-检查项-列表")
    String RM_CHECK_RULE_ITEM_LIST = "/rm/check/rule/item/list";
    @Desc("2. 删除检查规则-检查项-详情")
    String RM_CHECK_RULE_ITEM_INFO = "/rm/check/rule/item/info";
    @Desc("3. 删除检查规则-检查项-新增")
    String RM_CHECK_RULE_ITEM_CREATE = "/rm/check/rule/item/create";
    @Desc("4. 删除检查规则-检查项-更新")
    String RM_CHECK_RULE_ITEM_UPDATE = "/rm/check/rule/item/update";
    @Desc("5. 删除检查规则-检查项-移除")
    String RM_CHECK_RULE_ITEM_REMOVE = "/rm/check/rule/item/remove";


}
