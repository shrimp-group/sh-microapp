package com.wkclz.micro.liteflow.rest;


import com.wkclz.core.annotation.Router;

/**
 * Description:
 * Created: wangkaicun @ 2017-10-19 上午12:46
 */

@Router(module = "micro-liteflow", prefix = Route.PREFIX)
public interface Route {

    String PREFIX = "/micro-liteflow";

    String CHAIN_PAGE = "/chain/page";
    String CHAIN_INFO = "/chain/info";
    String CHAIN_CREATE = "/chain/create";
    String CHAIN_UPDATE = "/chain/update";
    String CHAIN_REMOVE = "/chain/remove";



    String SCRIPT_PAGE = "/script/page";
    String SCRIPT_INFO = "/script/info";
    String SCRIPT_CREATE = "/script/create";
    String SCRIPT_UPDATE = "/script/update";
    String SCRIPT_REMOVE = "/script/remove";


}
