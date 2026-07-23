package com.wkclz.micro.form.rest;


import com.wkclz.core.annotation.Router;

/**
 * Description:
 * Created: wangkaicun @ 2017-10-19 上午12:46
 */



@Router(module = "micro-form", prefix = Route.PREFIX)
public interface Route {

    String PREFIX = "/micro-form";



    String FORM_PAGE = "/form/page";
    String FORM_INFO = "/form/info";
    String FORM_CREATE = "/form/create";
    String FORM_UPDATE = "/form/update";
    String FORM_REMOVE = "/form/remove";


    String FORM_DB_COLUMNS = "/form/db/columns";


    // 客户端接入的相关接口

    String COMMON_FORM_LIST = "/common/form/list";
    String COMMON_FORM_DETAIL = "/common/form/detail";




    String COMMON_FORM_RULE = "/common/form/rule";


















    String FORM_RULE_VALIDATOR_TEMPLATE_PAGE = "/form/rule/validator/template/page";
    String FORM_RULE_VALIDATOR_TEMPLATE_INFO = "/form/rule/validator/template/info";
    String FORM_RULE_VALIDATOR_TEMPLATE_CREATE = "/form/rule/validator/template/create";
    String FORM_RULE_VALIDATOR_TEMPLATE_UPDATE = "/form/rule/validator/template/update";
    String FORM_RULE_VALIDATOR_TEMPLATE_REMOVE = "/form/rule/validator/template/remove";










    String FORM_RULE_PAGE = "/form/rule/page";
    String FORM_RULE_INFO = "/form/rule/info";
    String FORM_RULE_CREATE = "/form/rule/create";
    String FORM_RULE_UPDATE = "/form/rule/update";
    String FORM_RULE_REMOVE = "/form/rule/remove";



    String FORM_RULE_FIELD_AND_VALIDATOR = "/form/rule/field_and_validator";
    String FORM_RULE_FIELD_AND_VALIDATOR_SAVE = "/form/rule/field_and_validator/save";






}
