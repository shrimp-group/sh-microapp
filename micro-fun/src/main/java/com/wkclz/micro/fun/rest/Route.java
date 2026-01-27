package com.wkclz.micro.fun.rest;


import com.wkclz.core.annotation.Desc;
import com.wkclz.core.annotation.Router;

/**
 * Description:
 * Created: wangkaicun @ 2017-10-19 上午12:46
 */


@Router(module = "micro-fun", prefix = Route.PREFIX)
public interface Route {

    String PREFIX = "/micro-fun";


    @Desc("1. 函数分类-列表")
    String FUN_CATEGORY_LIST = "/fun/category/list";
    @Desc("2. 函数分类-树")
    String FUN_CATEGORY_TREE = "/fun/category/tree";
    @Desc("3. 函数分类-详情")
    String FUN_CATEGORY_INFO = "/fun/category/info";
    @Desc("4. 函数分类-创建")
    String FUN_CATEGORY_CREATE = "/fun/category/create";
    @Desc("5. 函数分类-修改")
    String FUN_CATEGORY_UPDATE = "/fun/category/update";
    @Desc("6. 函数分类-删除")
    String FUN_CATEGORY_REMOVE = "/fun/category/remove";
    @Desc("7. 函数分类-选项")
    String FUN_CATEGORY_OPTIONS = "/fun/category/options";


    @Desc("1. 函数体-分页")
    String FUN_FUNCTION_PAGE = "/fun/function/page";
    @Desc("2. 函数体-详情")
    String FUN_FUNCTION_INFO = "/fun/function/info";
    @Desc("3. 函数体-创建")
    String FUN_FUNCTION_CREATE = "/fun/function/create";
    @Desc("4. 函数体-修改")
    String FUN_FUNCTION_UPDATE = "/fun/function/update";
    @Desc("5. 函数体-删除")
    String FUN_FUNCTION_REMOVE = "/fun/function/remove";
    @Desc("6. 函数体-选项")
    String FUN_FUNCTION_OPTIONS = "/fun/function/options";
    @Desc("7. 函数体-测试")
    String FUN_FUNCTION_TEST = "/fun/function/test";

}
