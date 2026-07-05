package com.wkclz.micro.mask.rest;


import com.wkclz.core.annotation.ApiDesc;
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

    @ApiDesc("1. 脱敏规则-分页")
    String RULE_PAGE = "/rule/page";
    @ApiDesc("2. 脱敏规则-详情")
    String RULE_INFO = "/rule/info";
    @ApiDesc("3. 脱敏规则-新增")
    String RULE_CREATE = "/rule/create";
    @ApiDesc("4. 脱敏规则-修改")
    String RULE_UPDATE = "/rule/update";
    @ApiDesc("5. 脱敏规则-删除")
    String RULE_REMOVE = "/rule/remove";


    @ApiDesc("1. 脱敏规则-测试")
    String RULE_TEST = "/rule/test";
    @ApiDesc("2. 脱敏规则-验证")
    String RULE_VERIFY = "/rule/verify";

}
