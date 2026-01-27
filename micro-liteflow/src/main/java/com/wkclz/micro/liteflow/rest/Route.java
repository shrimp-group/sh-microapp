package com.wkclz.micro.liteflow.rest;


import com.wkclz.core.annotation.Desc;
import com.wkclz.core.annotation.Router;

/**
 * Description:
 * Created: wangkaicun @ 2017-10-19 上午12:46
 */

@Router(module = "micro-liteflow", prefix = Route.PREFIX)
public interface Route {

    String PREFIX = "/micro-liteflow";

    @Desc("1. liteflow-规则-分页")
    String CHAIN_PAGE = "/chain/page";
    @Desc("2. liteflow-规则-详情")
    String CHAIN_INFO = "/chain/info";
    @Desc("3. liteflow-规则-添加")
    String CHAIN_CREATE = "/chain/create";
    @Desc("4. liteflow-规则-更新")
    String CHAIN_UPDATE = "/chain/update";
    @Desc("5. liteflow-规则-删除")
    String CHAIN_REMOVE = "/chain/remove";



    @Desc("1. liteflow-脚本-分页")
    String SCRIPT_PAGE = "/script/page";
    @Desc("2. liteflow-脚本-详情")
    String SCRIPT_INFO = "/script/info";
    @Desc("3. liteflow-脚本-添加")
    String SCRIPT_CREATE = "/script/create";
    @Desc("4. liteflow-脚本-更新")
    String SCRIPT_UPDATE = "/script/update";
    @Desc("5. liteflow-脚本-删除")
    String SCRIPT_REMOVE = "/script/remove";


}
