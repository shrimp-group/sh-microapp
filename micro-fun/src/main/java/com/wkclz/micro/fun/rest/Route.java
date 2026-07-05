package com.wkclz.micro.fun.rest;


import com.wkclz.core.annotation.ApiDesc;
import com.wkclz.core.annotation.Router;

/**
 * Description:
 * Created: wangkaicun @ 2017-10-19 上午12:46
 */


@Router(module = "micro-fun", prefix = Route.PREFIX)
public interface Route {

    String PREFIX = "/micro-fun";


    @ApiDesc("1. 函数分类-列表")
    String FUN_CATEGORY_LIST = "/category/list";
    @ApiDesc("2. 函数分类-树")
    String FUN_CATEGORY_TREE = "/category/tree";
    @ApiDesc("3. 函数分类-详情")
    String FUN_CATEGORY_INFO = "/category/info";
    @ApiDesc("4. 函数分类-创建")
    String FUN_CATEGORY_CREATE = "/category/create";
    @ApiDesc("5. 函数分类-修改")
    String FUN_CATEGORY_UPDATE = "/category/update";
    @ApiDesc("6. 函数分类-删除")
    String FUN_CATEGORY_REMOVE = "/category/remove";
    @ApiDesc("7. 函数分类-选项")
    String FUN_CATEGORY_OPTIONS = "/category/options";


    @ApiDesc("1. 函数体-分页")
    String FUN_FUNCTION_PAGE = "/function/page";
    @ApiDesc("2. 函数体-详情")
    String FUN_FUNCTION_INFO = "/function/info";
    @ApiDesc("3. 函数体-创建")
    String FUN_FUNCTION_CREATE = "/function/create";
    @ApiDesc("4. 函数体-修改")
    String FUN_FUNCTION_UPDATE = "/function/update";
    @ApiDesc("5. 函数体-删除")
    String FUN_FUNCTION_REMOVE = "/function/remove";
    @ApiDesc("6. 函数体-选项")
    String FUN_FUNCTION_OPTIONS = "/function/options";
    @ApiDesc("7. 函数体-测试")
    String FUN_FUNCTION_TEST = "/function/test";

}
