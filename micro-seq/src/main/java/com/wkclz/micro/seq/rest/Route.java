package com.wkclz.micro.seq.rest;


import com.wkclz.core.annotation.Router;

/**
 * Description:
 * Created: wangkaicun @ 2017-10-19 上午12:46
 */

@Router(module = "micro-seq", prefix = Route.PREFIX)
public interface Route {

    String PREFIX = "/micro-seq";


    String SEQUENCE_PAGE = "/sequence/page";
    String SEQUENCE_INFO = "/sequence/info";
    String SEQUENCE_UPDATE = "/sequence/update";

}
