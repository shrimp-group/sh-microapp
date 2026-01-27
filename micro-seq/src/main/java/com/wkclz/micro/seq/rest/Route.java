package com.wkclz.micro.seq.rest;


import com.wkclz.core.annotation.Desc;
import com.wkclz.core.annotation.Router;

/**
 * Description:
 * Created: wangkaicun @ 2017-10-19 上午12:46
 */

@Router(module = "micro-sequence", prefix = Route.PREFIX)
public interface Route {

    String PREFIX = "/micro-sequence";


    @Desc("1. 序列生成-分页")
    String SEQUENCE_PAGE = "/sequence/page";
    @Desc("2. 序列生成-详情")
    String SEQUENCE_INFO = "/sequence/info";
    @Desc("3. 序列生成-修改")
    String SEQUENCE_UPDATE = "/sequence/update";

}
