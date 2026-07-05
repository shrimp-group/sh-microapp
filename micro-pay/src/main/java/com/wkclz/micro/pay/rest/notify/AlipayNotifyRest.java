package com.wkclz.micro.pay.rest.notify;

import com.alibaba.fastjson2.JSONObject;
import com.wkclz.micro.pay.helper.AlipayHelper;
import com.wkclz.micro.pay.bean.dto.OrderPayResult;
import com.wkclz.micro.pay.bean.entity.PayOrder;
import com.wkclz.micro.pay.bean.vo.AlipayNotify;
import com.wkclz.micro.pay.rest.Route;
import com.wkclz.micro.pay.service.PayOrderService;
import com.wkclz.micro.pay.spi.PayOrderSpi;
import com.wkclz.core.exception.ValidationException;
import com.wkclz.web.helper.RequestHelper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;


/**
 * Description: Create by Shrimp Generator
 * @author: wangkaicun @ current time
 * @table: pay_order (支付-订单)
 */
@Tag(name = "支付宝回调", description = "支付宝支付回调通知接口")
@Slf4j
@RestController
@RequestMapping(Route.PREFIX)
public class AlipayNotifyRest {

    /**
     * 支付通知
     */

    @Autowired(required = false)
    private PayOrderSpi payOrderSpi;
    @Autowired
    private AlipayHelper alipayHelper;
    @Autowired
    private PayOrderService payOrderService;


    @Operation(summary = "1. 支付宝异步通知/验签")
    @PostMapping(Route.PUBLIC_ALIPAY_NOTIFY_TENANT)
    @Transactional(rollbackFor = Exception.class)
    public void alipayNotify(
            HttpServletRequest req,
            HttpServletResponse rep,
            @PathVariable("tenantCode") String tenantCode,
            @PathVariable("appid") String appid
    ) {
        if (payOrderSpi == null) {
            throw ValidationException.of("订单交互服务未配置");
        }
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

        // 通过 SPI 更新订单支付成功状态
        try {
            OrderPayResult payResult = new OrderPayResult();
            payResult.setOrderNo(payOrder.getOrderNo());
            payResult.setPayMethod(payOrder.getPayMethod());
            payResult.setPayFlowNo(payOrder.getPayFlowNo());
            payResult.setPayTime(payOrder.getPayTime());
            payOrderSpi.updateOrderToPaid(payResult);
            log.info("支付宝支付成功，订单状态已更新为 PAID, orderNo: {}", payOrder.getOrderNo());
        } catch (Exception e) {
            log.error("支付宝支付成功，更新订单状态失败, orderNo: {}", payOrder.getOrderNo(), e);
        }

        log.error("success!");
        AlipayHelper.printBack(rep, "success!");
    }


}
