package com.wkclz.micro.pay.rest.notify;

import com.github.binarywang.wxpay.bean.notify.SignatureHeader;
import com.github.binarywang.wxpay.bean.notify.WxPayNotifyResponse;
import com.github.binarywang.wxpay.bean.notify.WxPayNotifyV3Result;
import com.github.binarywang.wxpay.bean.notify.WxPayRefundNotifyV3Result;
import com.github.binarywang.wxpay.constant.WxPayConstants;
import com.github.binarywang.wxpay.exception.WxPayException;
import com.github.binarywang.wxpay.service.WxPayService;
import com.wkclz.micro.pay.cache.WxpayClientCache;
import com.wkclz.micro.pay.helper.WxpayHelper;
import com.wkclz.micro.pay.bean.entity.PayOrder;
import com.wkclz.micro.pay.rest.Route;
import com.wkclz.micro.pay.service.PayOrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;


/**
 * Description: Create by Shrimp Generator
 * @author: wangkaicun @ current time
 * @table: pay_order (支付-订单)
 */
@Tag(name = "微信支付回调", description = "微信支付回调通知接口")
@Slf4j
@RestController
@RequestMapping(Route.PREFIX)
@Validated
public class WxpayNotifyRest {

    /**
     * 支付通知
     */

    @Resource
    private WxpayHelper wxpayHelper;
    @Resource
    private PayOrderService payOrderService;
    @Resource
    private WxpayClientCache wxpayClientCache;



    @Operation(summary = "1. 微信支付异步通知")
    @PostMapping(Route.PUBLIC_WXPAY_NOTIFY_TENANT)
    @Transactional(rollbackFor = Exception.class)
    public String publicWxpayNotifyTenant(
        HttpServletRequest req,
        @PathVariable("tenantCode") String tenantCode,
        @PathVariable("appid") String appid,
        @RequestBody String jsonData
    ) throws WxPayException {
        SignatureHeader requestHeader = WxpayHelper.getRequestHeader(req);
        log.info("微信支付 tenantCode: {}, appid: {} 回调： {}, header: {}", tenantCode, appid, jsonData, requestHeader);

        WxPayService wxPayService = wxpayClientCache.getClient(tenantCode);
        WxPayNotifyV3Result notifyResult = wxPayService.parseOrderNotifyV3Result(jsonData, requestHeader);

        /*
        WxPayOrderNotifyResult notifyResult = wxPayService.parseOrderNotifyResult(xmlData);

        if (!"SUCCESS".equals(notifyResult.getReturnCode())) {
            log.error("支付回调状态失败：{}", notifyResult.getReturnMsg());
            return WxPayNotifyResponse.fail("支付回调状态失败：" + notifyResult.getReturnMsg());
        }
        if (!"SUCCESS".equals(notifyResult.getResultCode())) {
            log.error("支付回调状态失败：'ErrCode':'{}', ErrCodeDes':'{}'", notifyResult.getErrCode(), notifyResult.getErrCodeDes());
            return WxPayNotifyResponse.fail("支付回调状态失败：" + notifyResult.getErrCodeDes());
        }
        String outTradeNo = notifyResult.getOutTradeNo();
        log.info("----------> 支付成功，{}", outTradeNo);
        */
        WxPayNotifyV3Result.DecryptNotifyResult result = notifyResult.getResult();
        String outTradeNo = result.getOutTradeNo();

        if (StringUtils.isBlank(outTradeNo)) {
            log.error("outTradeNo is null, please check!");
            return WxPayNotifyResponse.fail("支付回调，订单号不存在!");
        }

        if (!WxPayConstants.WxpayTradeStatus.SUCCESS.equals(result.getTradeState())) {
            log.error("支付未成功: {}", outTradeNo);
            return WxPayNotifyResponse.fail(outTradeNo + ": 支付未成功!");
        }

        PayOrder payOrder = payOrderService.getPayOrderByOutTradeNo(outTradeNo);
        if (payOrder == null) {
            log.error("outTradeNo is error, please check!");
            return WxPayNotifyResponse.fail("支付回调，订单不存在!");
        }

        payOrder = wxpayHelper.payNotify(payOrder, result);
        if (payOrder == null) {
            return WxPayNotifyResponse.success("success");
        }
        payOrderService.update(payOrder);
        // TODO 写流水
        return WxPayNotifyResponse.success("success");
    }

    @Operation(summary = "2. 微信退款异步通知")
    @PostMapping(Route.PUBLIC_WXPAY_REFUND_NOTIFY_TENANT)
    @Transactional(rollbackFor = Exception.class)
    public String publicWxpayRefundNotifyTenant(
        HttpServletRequest req,
        @PathVariable("tenantCode") String tenantCode,
        @PathVariable("appid") String appid,
        @RequestBody String jsonData
    ) throws WxPayException {
        SignatureHeader requestHeader = WxpayHelper.getRequestHeader(req);


        log.info("微信退款 tenantCode: {}, appid: {} 回调： {}, header: {}", tenantCode, appid, jsonData, requestHeader);

        WxPayService wxPayService = wxpayClientCache.getClient(tenantCode);
        WxPayRefundNotifyV3Result refundNotifyResult = wxPayService.parseRefundNotifyV3Result(jsonData, requestHeader);

        WxPayRefundNotifyV3Result.DecryptNotifyResult result = refundNotifyResult.getResult();
        String s = wxpayHelper.wxRefundNotify(refundNotifyResult);
        return WxPayNotifyResponse.fail(s);
    }



}
