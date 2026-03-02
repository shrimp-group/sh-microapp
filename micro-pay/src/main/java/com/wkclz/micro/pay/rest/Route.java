package com.wkclz.micro.pay.rest;


import com.wkclz.core.annotation.Desc;
import com.wkclz.core.annotation.Router;

/**
 * Description:
 * Created: wangkaicun @ 2017-10-19 上午12:46
 */
@Router(module = "micro-pay", prefix = Route.PREFIX)
public interface Route {

    String PREFIX = "";

    /**
     * 微信支付配置
     */

    @Desc("1. 微信支付-分页")
    String WXPAY_CONFIG_PAGE = "/wxpay/config/page";
    @Desc("2. 微信支付-详情")
    String WXPAY_CONFIG_INFO = "/wxpay/config/info";
    @Desc("3. 微信支付-创建")
    String WXPAY_CONFIG_CREATE = "/wxpay/config/create";
    @Desc("4. 微信支付-更新")
    String WXPAY_CONFIG_UPDATE = "/wxpay/config/update";
    @Desc("5. 微信支付-移除")
    String WXPAY_CONFIG_REMOVE = "/wxpay/config/remove";
    @Desc("6. 微信支付配置域名安全验证")
    String WXPAY_CONFIG_VERIFY = "/wxpay/config/verify/MP_verify_{verifySign}.txt";

    /**
     * 支付宝支付配置
     */

    @Desc("1. 支付宝支付-分页")
    String ALIPAY_CONFIG_PAGE = "/alipay/config/page";
    @Desc("2. 支付宝支付-详情")
    String ALIPAY_CONFIG_INFO = "/alipay/config/info";
    @Desc("3. 支付宝支付-创建")
    String ALIPAY_CONFIG_CREATE = "/alipay/config/create";
    @Desc("4. 支付宝支付-更新")
    String ALIPAY_CONFIG_UPDATE = "/alipay/config/update";
    @Desc("5. 支付宝支付-移除")
    String ALIPAY_CONFIG_REMOVE = "/alipay/config/remove";




    @Desc("1. 模拟支付")
    String COMMON_PAYORDER_MOCK_PAY = "/common/payorder/mock/pay";
    @Desc("2. 支付订单状态查询")
    String COMMON_PAYORDER_STATUS = "/common/payorder/status";


    // 管理端接口
    @Desc("1. 支付订单-退款申请")
    String PAYORDER_REFUND = "/payorder/refund/apply";


    /**
     * 通知类接口
     */

    @Desc("1. public-【支付宝】异步通知/验签")
    String PUBLIC_ALIPAY_NOTIFY_TENANT = "/public/alipay/notify/{tenantCode}/{appid}";
    @Desc("2. public-【微信支付】异步通知")
    String PUBLIC_WXPAY_NOTIFY_TENANT = "/public/wxpay/notify/{tenantCode}/{appid}";
    @Desc("3. public-【微信支付】退款异步通知")
    String PUBLIC_WXPAY_REFUND_NOTIFY_TENANT = "/public/wxpay/refund/notify/{tenantCode}/{appid}";



}
