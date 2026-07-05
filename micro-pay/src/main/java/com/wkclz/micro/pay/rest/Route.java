package com.wkclz.micro.pay.rest;


import com.wkclz.core.annotation.ApiDesc;
import com.wkclz.core.annotation.Router;

/**
 * Description:
 * Created: wangkaicun @ 2017-10-19 上午12:46
 */
@Router(module = "micro-pay", prefix = Route.PREFIX)
public interface Route {

    String PREFIX = "/micro-pay";

    /**
     * 微信支付配置
     */

    @ApiDesc("1. 微信支付-分页")
    String WXPAY_CONFIG_PAGE = "/wxpay/config/page";
    @ApiDesc("2. 微信支付-详情")
    String WXPAY_CONFIG_INFO = "/wxpay/config/info";
    @ApiDesc("3. 微信支付-创建")
    String WXPAY_CONFIG_CREATE = "/wxpay/config/create";
    @ApiDesc("4. 微信支付-更新")
    String WXPAY_CONFIG_UPDATE = "/wxpay/config/update";
    @ApiDesc("5. 微信支付-移除")
    String WXPAY_CONFIG_REMOVE = "/wxpay/config/remove";
    @ApiDesc("6. 微信支付配置域名安全验证")
    String WXPAY_CONFIG_VERIFY = "/wxpay/config/verify/MP_verify_{verifySign}.txt";

    /**
     * 支付宝支付配置
     */

    @ApiDesc("1. 支付宝支付-分页")
    String ALIPAY_CONFIG_PAGE = "/alipay/config/page";
    @ApiDesc("2. 支付宝支付-详情")
    String ALIPAY_CONFIG_INFO = "/alipay/config/info";
    @ApiDesc("3. 支付宝支付-创建")
    String ALIPAY_CONFIG_CREATE = "/alipay/config/create";
    @ApiDesc("4. 支付宝支付-更新")
    String ALIPAY_CONFIG_UPDATE = "/alipay/config/update";
    @ApiDesc("5. 支付宝支付-移除")
    String ALIPAY_CONFIG_REMOVE = "/alipay/config/remove";




    @ApiDesc("1. 模拟支付")
    String COMMON_PAYORDER_PAY_MOCK = "/common/payorder/pay/mock";
    @ApiDesc("2. 发起支付")
    String COMMON_PAYORDER_PAY = "/common/payorder/pay";
    @ApiDesc("3. 支付订单状态查询")
    String COMMON_PAYORDER_STATUS = "/common/payorder/status";


    // 管理端接口
    @ApiDesc("1. 支付订单-退款申请")
    String PAYORDER_REFUND = "/payorder/refund/apply";


    /**
     * 通知类接口
     */

    @ApiDesc("1. public-【支付宝】异步通知/验签")
    String PUBLIC_ALIPAY_NOTIFY_TENANT = "/public/alipay/notify/{tenantCode}/{appid}";
    @ApiDesc("2. public-【微信支付】异步通知")
    String PUBLIC_WXPAY_NOTIFY_TENANT = "/public/wxpay/notify/{tenantCode}/{appid}";
    @ApiDesc("3. public-【微信支付】退款异步通知")
    String PUBLIC_WXPAY_REFUND_NOTIFY_TENANT = "/public/wxpay/refund/notify/{tenantCode}/{appid}";



}
