package com.wkclz.micro.dict.rest;


import com.wkclz.core.annotation.Router;

/**
 * Description:
 * Created: wangkaicun @ 2017-10-19 上午12:46
 */


@Router(module = "micro-dict", prefix = Route.PREFIX)
public interface Route {

    String PREFIX = "/micro-dict";

    // DICT 字典管理
    String DICT_PAGE = "/dict/page";
    String DICT_INFO = "/dict/info";
    String DICT_CREATE = "/dict/create";
    String DICT_UPDATE = "/dict/update";
    String DICT_REMOVE = "/dict/remove";
    String DICT_COPY = "/dict/copy";
    String DICT_PASTE = "/dict/paste";
    String DICT_OPTIONS = "/dict/options";


    // DICT_ITEM 字典枚举管理
    String DICT_ITEM_LIST = "/dict/item/list";
    String DICT_ITEM_SAVE = "/dict/item/save";

    // COMMON_DICT 字典
    String COMMON_DICT_LIST = "/common/dict/list";
    String COMMON_DICTS_LIST = "/common/dicts/list";

}
