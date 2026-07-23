package com.wkclz.micro.fun.rest;


import com.wkclz.core.annotation.Router;

/**
 * Description:
 * Created: wangkaicun @ 2017-10-19 上午12:46
 */


@Router(module = "micro-fun", prefix = Route.PREFIX)
public interface Route {

    String PREFIX = "/micro-fun";


    String FUN_CATEGORY_LIST = "/category/list";
    String FUN_CATEGORY_TREE = "/category/tree";
    String FUN_CATEGORY_INFO = "/category/info";
    String FUN_CATEGORY_CREATE = "/category/create";
    String FUN_CATEGORY_UPDATE = "/category/update";
    String FUN_CATEGORY_REMOVE = "/category/remove";
    String FUN_CATEGORY_OPTIONS = "/category/options";


    String FUN_FUNCTION_PAGE = "/function/page";
    String FUN_FUNCTION_INFO = "/function/info";
    String FUN_FUNCTION_CREATE = "/function/create";
    String FUN_FUNCTION_UPDATE = "/function/update";
    String FUN_FUNCTION_REMOVE = "/function/remove";
    String FUN_FUNCTION_OPTIONS = "/function/options";
    String FUN_FUNCTION_TEST = "/function/test";

}
