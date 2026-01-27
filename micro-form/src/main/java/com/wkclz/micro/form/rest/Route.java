package com.wkclz.micro.form.rest;


import com.wkclz.core.annotation.Desc;
import com.wkclz.core.annotation.Router;

/**
 * Description:
 * Created: wangkaicun @ 2017-10-19 上午12:46
 */



@Router(module = "micro-form", prefix = Route.PREFIX)
public interface Route {

    String PREFIX = "/micro-form";



    @Desc("01. 表单-分页")
    String FORM_PAGE = "/form/page";
    @Desc("02. 表单-详情")
    String FORM_INFO = "/form/info";
    @Desc("03. 表单创建")
    String FORM_CREATE = "/form/create";
    @Desc("04. 表单-修改")
    String FORM_UPDATE = "/form/update";
    @Desc("05. 表单-删除")
    String FORM_REMOVE = "/form/remove";


    @Desc("07. 表单输入项-数据库字段")
    String FORM_DB_COLUMNS = "/form/db/columns";


    // 客户端接入的相关接口

    @Desc("11. common-表单列表【用于生成下拉选项】")
    String COMMON_FORM_LIST = "/common/form/list";
    @Desc("12. customer-表单详情【用于构造输入表单】")
    String COMMON_FORM_DETAIL = "/common/form/detail";









    @Desc("1. 表单校验规则-分页")
    String FORM_RULE_PAGE = "/form/rule/page";
    @Desc("2. 表单校验规则-详情")
    String FORM_RULE_INFO = "/form/rule/info";
    @Desc("3. 表单校验规则-新增")
    String FORM_RULE_CREATE = "/form/rule/create";
    @Desc("4. 表单校验规则-更新")
    String FORM_RULE_UPDATE = "/form/rule/update";
    @Desc("5. 表单校验规则-移除")
    String FORM_RULE_REMOVE = "/form/rule/remove";

    @Desc("1. 表单校验规则-检查项-列表")
    String FORM_RULE_ITEM_LIST = "/form/rule/item/list";
    @Desc("2. 表单校验规则-检查项-详情")
    String FORM_RULE_ITEM_INFO = "/form/rule/item/info";
    @Desc("3. 表单校验规则-检查项-新增")
    String FORM_RULE_ITEM_CREATE = "/form/rule/item/create";
    @Desc("4. 表单校验规则-检查项-更新")
    String FORM_RULE_ITEM_UPDATE = "/form/rule/item/update";
    @Desc("5. 表单校验规则-检查项-移除")
    String FORM_RULE_ITEM_REMOVE = "/form/rule/item/remove";

    @Desc("1. 表单校验规则-验证规则")
    String COMMON_FORM_RULE = "/common/form/rule";


}
