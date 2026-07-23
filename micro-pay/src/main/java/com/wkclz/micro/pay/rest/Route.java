package com.wkclz.micro.pay.rest;


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

    String WXPAY_CONFIG_PAGE = "/wxpay/config/page";
    String WXPAY_CONFIG_INFO = "/wxpay/config/info";
    String WXPAY_CONFIG_CREATE = "/wxpay/config/create";
    String WXPAY_CONFIG_UPDATE = "/wxpay/config/update";
    String WXPAY_CONFIG_REMOVE = "/wxpay/config/remove";
    String WXPAY_CONFIG_VERIFY = "/wxpay/config/verify/MP_verify_{verifySign}.txt";

    /**
     * 支付宝支付配置
     */

    String ALIPAY_CONFIG_PAGE = "/alipay/config/page";
    String ALIPAY_CONFIG_INFO = "/alipay/config/info";
    String ALIPAY_CONFIG_CREATE = "/alipay/config/create";
    String ALIPAY_CONFIG_UPDATE = "/alipay/config/update";
    String ALIPAY_CONFIG_REMOVE = "/alipay/config/remove";




    String COMMON_PAYORDER_PAY_MOCK = "/common/payorder/pay/mock";
    String COMMON_PAYORDER_PAY = "/common/payorder/pay";
    String COMMON_PAYORDER_STATUS = "/common/payorder/status";


    // 管理端接口
    String PAYORDER_REFUND = "/payorder/refund/apply";


    /**
     * 通知类接口
     */

    String PUBLIC_ALIPAY_NOTIFY_TENANT = "/public/alipay/notify/{tenantCode}/{appid}";
    String PUBLIC_WXPAY_NOTIFY_TENANT = "/public/wxpay/notify/{tenantCode}/{appid}";
    String PUBLIC_WXPAY_REFUND_NOTIFY_TENANT = "/public/wxpay/refund/notify/{tenantCode}/{appid}";



}
