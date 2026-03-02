package com.wkclz.micro.mask.rest;


import com.wkclz.core.annotation.Desc;
import com.wkclz.core.annotation.Router;

/**
 * Description:
 * Created: wangkaicun @ 2017-10-19 上午12:46
 */
@Router(module = "micro-mask", prefix = Route.PREFIX)
public interface Route {

    String PREFIX = "/micro-mask";



    /**
     * 脱敏规则配置
     */

    @Desc("1. 脱敏规则-分页")
    String RULE_PAGE = "/rule/page";
    @Desc("2. 脱敏规则-详情")
    String RULE_INFO = "/rule/info";
    @Desc("3. 脱敏规则-新增")
    String RULE_CREATE = "/rule/create";
    @Desc("4. 脱敏规则-修改")
    String RULE_UPDATE = "/rule/update";
    @Desc("5. 脱敏规则-删除")
    String RULE_REMOVE = "/rule/remove";


    @Desc("1. 脱敏规则-测试")
    String RULE_TEST = "/rule/test";
    @Desc("2. 脱敏规则-验证")
    String RULE_VERIFY = "/rule/verify";

}
