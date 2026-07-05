package com.wkclz.micro.form.rest;


import com.wkclz.core.annotation.ApiDesc;
import com.wkclz.core.annotation.Router;

/**
 * Description:
 * Created: wangkaicun @ 2017-10-19 上午12:46
 */



@Router(module = "micro-form", prefix = Route.PREFIX)
public interface Route {

    String PREFIX = "/micro-form";



    @ApiDesc("01. 表单-分页")
    String FORM_PAGE = "/form/page";
    @ApiDesc("02. 表单-详情")
    String FORM_INFO = "/form/info";
    @ApiDesc("03. 表单创建")
    String FORM_CREATE = "/form/create";
    @ApiDesc("04. 表单-修改")
    String FORM_UPDATE = "/form/update";
    @ApiDesc("05. 表单-删除")
    String FORM_REMOVE = "/form/remove";


    @ApiDesc("07. 表单输入项-数据库字段")
    String FORM_DB_COLUMNS = "/form/db/columns";


    // 客户端接入的相关接口

    @ApiDesc("11. common-表单列表【用于生成下拉选项】")
    String COMMON_FORM_LIST = "/common/form/list";
    @ApiDesc("12. customer-表单详情【用于构造输入表单】")
    String COMMON_FORM_DETAIL = "/common/form/detail";




    @ApiDesc("1. 表单校验规则-验证规则")
    String COMMON_FORM_RULE = "/common/form/rule";


















    @ApiDesc("1. 表单校验规则模板-分页")
    String FORM_RULE_VALIDATOR_TEMPLATE_PAGE = "/form/rule/validator/template/page";
    @ApiDesc("2. 表单校验规则模板-详情")
    String FORM_RULE_VALIDATOR_TEMPLATE_INFO = "/form/rule/validator/template/info";
    @ApiDesc("3. 表单校验规则模板-新增")
    String FORM_RULE_VALIDATOR_TEMPLATE_CREATE = "/form/rule/validator/template/create";
    @ApiDesc("4. 表单校验规则模板-更新")
    String FORM_RULE_VALIDATOR_TEMPLATE_UPDATE = "/form/rule/validator/template/update";
    @ApiDesc("5. 表单校验规则模板-移除")
    String FORM_RULE_VALIDATOR_TEMPLATE_REMOVE = "/form/rule/validator/template/remove";










    @ApiDesc("1. 表单校验规则-分页")
    String FORM_RULE_PAGE = "/form/rule/page";
    @ApiDesc("2. 表单校验规则-详情")
    String FORM_RULE_INFO = "/form/rule/info";
    @ApiDesc("3. 表单校验规则-新增")
    String FORM_RULE_CREATE = "/form/rule/create";
    @ApiDesc("4. 表单校验规则-更新")
    String FORM_RULE_UPDATE = "/form/rule/update";
    @ApiDesc("5. 表单校验规则-移除")
    String FORM_RULE_REMOVE = "/form/rule/remove";



    @ApiDesc("6. 表单校验规则-字段及验证器")
    String FORM_RULE_FIELD_AND_VALIDATOR = "/form/rule/field_and_validator";
    @ApiDesc("7. 表单校验规则-字段及验证器-保存")
    String FORM_RULE_FIELD_AND_VALIDATOR_SAVE = "/form/rule/field_and_validator/save";






}
