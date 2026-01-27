package com.wkclz.micro.pay.rest.notify;

import com.alibaba.fastjson2.JSONObject;
import com.wkclz.micro.pay.helper.AlipayHelper;
import com.wkclz.micro.pay.pojo.entity.PayOrder;
import com.wkclz.micro.pay.pojo.vo.AlipayNotify;
import com.wkclz.micro.pay.rest.Route;
import com.wkclz.micro.pay.service.PayOrderService;
import com.wkclz.micro.pay.spi.PayNoticeSpi;
import com.wkclz.web.helper.RequestHelper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;


/**
 * Description: Create by Shrimp Generator
 * @author: wangkaicun @ current time
 * @table: pay_order (支付-订单)
 */
@Slf4j
@RestController
public class AlipayNotifyRest {

    /**
     * 支付通知
     */

    @Autowired(required = false)
    private PayNoticeSpi payNoticeSpi;
    @Autowired
    private AlipayHelper alipayHelper;
    @Autowired
    private PayOrderService payOrderService;


    /**
     * @api {post} /public/alipay/notify 1. public-【支付宝】异步通知/验签
     * @apiGroup PUBLIC
     *
     * @apiVersion 0.0.1
     *
     */
    @PostMapping(Route.PUBLIC_ALIPAY_NOTIFY_TENANT)
    @Transactional(rollbackFor = Exception.class)
    public void alipayNotify(
            HttpServletRequest req,
            HttpServletResponse rep,
            @PathVariable("tenantCode") String tenantCode,
            @PathVariable("appid") String appid
    ) {
        Map<String, String> params = RequestHelper.getParamsFromRequest(req);
        String paramsString = JSONObject.toJSONString(params);
        log.info("支付宝支付 tenantCode: {}, appid: {}, 参数： {}", tenantCode, appid, paramsString);

        // 验签
        boolean b = alipayHelper.signVerifie(rep, params, tenantCode);
        if (!b) {
            return;
        }
        AlipayNotify notify = JSONObject.parseObject(paramsString, AlipayNotify.class);

        // 订单处理
        String outTradeNo = notify.getOutTradeNo();
        if (StringUtils.isBlank(outTradeNo)) {
            log.error("outTradeNo is null, please check!");
            AlipayHelper.printBack(rep, "outTradeNo is null!");
            return;
        }
        PayOrder payOrder = payOrderService.getPayOrderByOutTradeNo(outTradeNo);
        if (payOrder == null) {
            log.error("outTradeNo is error, please check!");
            AlipayHelper.printBack(rep, "utTradeNo is error!");
            return;
        }

        payOrder = alipayHelper.payNotify(req, rep, payOrder, notify);
        if (payOrder == null) {
            log.warn("Repeat processing, please check!");
            AlipayHelper.printBack(rep, "Repeat processing!");
            return;
        }

        payOrderService.update(payOrder);
        // TODO 写流水

        if (payNoticeSpi != null) {
            payNoticeSpi.payidNotice(payOrder);
        }
        log.error("success!");
        AlipayHelper.printBack(rep, "success!");
    }


}
