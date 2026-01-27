package com.wkclz.micro.dict.rest;


import com.wkclz.core.annotation.Desc;
import com.wkclz.core.annotation.Router;

/**
 * Description:
 * Created: wangkaicun @ 2017-10-19 上午12:46
 */


@Router(module = "micro-dict", prefix = Route.PREFIX)
public interface Route {

    String PREFIX = "/micro-dict";

    // DICT 字典管理
    @Desc("1. 字典类型列表分页")
    String DICT_PAGE = "/dict/page";
    @Desc("2. 字典类型列表详情")
    String DICT_INFO = "/dict/info";
    @Desc("3. 字典类型添加")
    String DICT_CREATE = "/dict/create";
    @Desc("4. 字典类型修改")
    String DICT_UPDATE = "/dict/update";
    @Desc("5. 字典类型删除")
    String DICT_REMOVE = "/dict/remove";
    @Desc("6. 字典-COPY")
    String DICT_COPY = "/dict/copy";
    @Desc("7. 字典-PASTE")
    String DICT_PASTE = "/dict/paste";
    @Desc("8. 字典-清单")
    String DICT_OPTIONS = "/dict/options";


    // DICT_ITEM 字典枚举管理
    @Desc("1. 字典内容列表（不分页）")
    String DICT_ITEM_LIST = "/dict/item/list";
    @Desc("2. 字典内容保存")
    String DICT_ITEM_SAVE = "/dict/item/save";

    // COMMON_DICT 字典
    @Desc("1. 字典内容列表（单字典,不分页）")
    String COMMON_DICT_LIST = "/common/dict/list";
    @Desc("2. 字典内容列表（多字典,不分页）")
    String COMMON_DICTS_LIST = "/common/dicts/list";

}
