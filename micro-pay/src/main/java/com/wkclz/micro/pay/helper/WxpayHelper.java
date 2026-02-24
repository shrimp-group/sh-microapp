package com.wkclz.micro.pay.helper;

import cn.hutool.core.date.DateField;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.text.StrFormatter;
import com.alibaba.fastjson2.JSONObject;
import com.github.binarywang.wxpay.bean.notify.SignatureHeader;
import com.github.binarywang.wxpay.bean.notify.WxPayNotifyResponse;
import com.github.binarywang.wxpay.bean.notify.WxPayNotifyV3Result;
import com.github.binarywang.wxpay.bean.notify.WxPayRefundNotifyV3Result;
import com.github.binarywang.wxpay.bean.request.WxPayOrderCloseRequest;
import com.github.binarywang.wxpay.bean.request.WxPayOrderQueryV3Request;
import com.github.binarywang.wxpay.bean.request.WxPayRefundV3Request;
import com.github.binarywang.wxpay.bean.request.WxPayUnifiedOrderV3Request;
import com.github.binarywang.wxpay.bean.result.WxPayOrderQueryV3Result;
import com.github.binarywang.wxpay.bean.result.WxPayRefundV3Result;
import com.github.binarywang.wxpay.bean.result.WxPayUnifiedOrderV3Result;
import com.github.binarywang.wxpay.bean.result.enums.TradeTypeEnum;
import com.github.binarywang.wxpay.config.WxPayConfig;
import com.github.binarywang.wxpay.exception.WxPayException;
import com.github.binarywang.wxpay.service.WxPayService;
import com.wkclz.core.base.R;
import com.wkclz.core.exception.ValidationException;
import com.wkclz.iam.sdk.helper.SessionHelper;
import com.wkclz.micro.pay.cache.WxpayClientCache;
import com.wkclz.micro.pay.pojo.dto.PayOrderDto;
import com.wkclz.micro.pay.pojo.entity.PayOrder;
import com.wkclz.micro.pay.pojo.entity.PayWxpayConfig;
import com.wkclz.micro.pay.pojo.enums.PayStatus;
import com.wkclz.micro.pay.service.PayOrderService;
import com.wkclz.micro.pay.spi.PayNoticeSpi;
import com.wkclz.tool.tools.RsaTool;
import com.wkclz.web.helper.IpHelper;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.security.PrivateKey;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @author shrimp
 */

@Slf4j
@Component
public class WxpayHelper {

    @Autowired(required = false)
    private PayNoticeSpi payNoticeSpi;
    @Resource
    private PayOrderService payOrderService;
    @Resource
    private WxpayClientCache wxpayClientCache;

    public PayOrderDto pay(PayOrder payOrder, HttpServletRequest req, HttpServletResponse rep) {
        String openId = "openId"; // TODO SessionHelper.getUserSession().getOpenId();
        if (StringUtils.isBlank(openId)) {
            throw ValidationException.of("非微信登录，无法支付");
        }
        WxPayService wxPayService = wxpayClientCache.getClient(payOrder.getTenantCode());
        WxPayConfig config = wxPayService.getConfig();

        WxPayUnifiedOrderV3Request orderRequest = new WxPayUnifiedOrderV3Request();
        orderRequest.setAppid(config.getAppId());
        orderRequest.setMchid(config.getMchId());
        orderRequest.setDescription(payOrder.getBody());
        orderRequest.setOutTradeNo(payOrder.getOutTradeNo());
        DateTime timeExpire = DateUtil.offset(new Date(), DateField.HOUR_OF_DAY, 24);
        orderRequest.setTimeExpire(DateUtil.format(timeExpire, "yyyy-MM-dd'T'HH:mm:ssXXX"));

        // 需要替换 tenantCode, appid
        String notifyUrl = config.getNotifyUrl();
        notifyUrl = notifyUrl.replace("{tenantCode}", payOrder.getTenantCode());
        notifyUrl = notifyUrl.replace("{appid}", config.getAppId());
        orderRequest.setNotifyUrl(notifyUrl);

        // 价格信息
        WxPayUnifiedOrderV3Request.Amount amount = new WxPayUnifiedOrderV3Request.Amount();
        amount.setTotal(payOrder.getPaymentAmount().multiply(new BigDecimal("100")).intValue());
        // amount.setCurrency("CNY"); // 选填 string(16)
        orderRequest.setAmount(amount);

        // 用户信息
        WxPayUnifiedOrderV3Request.Payer payer = new WxPayUnifiedOrderV3Request.Payer();
        payer.setOpenid(openId);
        orderRequest.setPayer(payer);

        // 客户端信息
        WxPayUnifiedOrderV3Request.SceneInfo sceneInfo = new WxPayUnifiedOrderV3Request.SceneInfo();
        orderRequest.setSceneInfo(sceneInfo);
        sceneInfo.setPayerClientIp(IpHelper.getOriginIp(req));

        WxPayUnifiedOrderV3Result appResult = null;

        try {
            appResult = wxPayService.unifiedOrderV3(TradeTypeEnum.JSAPI, orderRequest);
            // appResult = wxService.createOrderV3(TradeTypeEnum.H5, orderRequest);
        } catch (WxPayException e) {
            log.error(e.getMessage(), e);
            throw ValidationException.of(e.getMessage());
        }
        if (appResult == null) {
            throw ValidationException.of("系统错误，发起支付失败");
        }
        PayOrderDto dto = PayOrderDto.copy(payOrder);
        dto.setWxpayUrl(appResult.getPrepayId());

        PrivateKey privateKey = RsaTool.convertToPrivateKey(config.getPrivateKeyString());
        Object payInfo = appResult.getPayInfo(TradeTypeEnum.JSAPI, config.getAppId(), config.getMchId(), privateKey);
        Map<String, Object> jsapiResult = JSONObject.parseObject(JSONObject.toJSONString(payInfo), Map.class);
        jsapiResult.put("prepayId", appResult.getPrepayId());
        dto.setJsapiResult(jsapiResult);
        return dto;

        /*
        if (!"SUCCESS".equals(appResult.getReturnCode())) {
            throw ValidationException.of("发起支付失败：" + wxResult.getReturnMsg());
        }
        if (!"SUCCESS".equals(wxResult.getResultCode())) {
            throw ValidationException.of("发起支付失败：'ErrCode':'" + wxResult.getErrCode() + "', ErrCodeDes':'" + wxResult.getErrCodeDes() + "'");
        }

        // String prepayId = wxResult.getPrepayId();
        Map<String, String> map = new HashMap<>();
        if (terminalType == TerminalType.PC) {
            String codeURL = wxResult.getCodeURL();
            map.put("codeURL", codeURL);

            String base64QRCode = QrCodeUtil.createBase64QrCode(codeURL);
            map.put("codeImg", base64QRCode);
        }
        if (terminalType == TerminalType.H5) {
            map.put("mwebUrl", wxResult.getMwebUrl());
        }
        if (terminalType == TerminalType.MINIAPP) {
            String time = System.currentTimeMillis() + "";
            map.put("appId", wxResult.getAppid());
            map.put("timeStamp", time.substring(0, 10));
            map.put("nonceStr", SecureUtil.md5(time));
            map.put("package", "prepay_id=" + wxResult.getPrepayId());
            map.put("signType", WxPayConstants.SignType.MD5);

            StringBuffer sb = new StringBuffer();
            sb.append("appId=").append(map.get("appId")).append("&")
                .append("nonceStr=").append(map.get("nonceStr")).append("&")
                .append("package=").append(map.get("package")).append("&")
                .append("signType=").append(map.get("signType")).append("&")
                .append("timeStamp=").append(map.get("timeStamp")).append("&")
                .append("key=").append(wxService.getConfig().getMchKey());
            map.put("paySign", SecureUtil.md5(sb.toString()).toUpperCase());
        }

        map.put("orderNo", payOrder.getOrderNo());
        map.put("orderDesc", payOrder.getBody());
        map.put("paymentAmount", payOrder.getPaymentAmount().toString());

        PayOrderDto dto = PayOrderDto.copy(payOrder);
        dto.setWxpayMap(map);
        return dto;
        */
    }


    /**
     * 只需要向微信关闭订单即可。本地订单会用定时器更新成关闭状态
     */
    public R payClose(PayOrder payOrder) {
        WxPayService wxPayService = wxpayClientCache.getClient(payOrder.getTenantCode());

        WxPayOrderQueryV3Request queryRequest = new WxPayOrderQueryV3Request();
        queryRequest.setOutTradeNo(payOrder.getOutTradeNo());

        WxPayOrderQueryV3Result queryResult = null;
        try {
            queryResult = wxPayService.queryOrderV3(queryRequest);
        } catch (WxPayException e) {
            log.error("支付状态查询失败：'ErrCode':'{}', ErrCodeDes':'{}'", e.getErrCode(), e.getErrCodeDes());
            return R.error("支付状态查询失败：" + e.getErrCodeDes());
        }
        /*
        if (!"SUCCESS".equals(queryResult.getReturnCode())) {
            log.error("支付状态查询失败：{}", queryResult.getReturnMsg());
            return R.error("支付状态查询失败：" + queryResult.getReturnMsg());
        }
        if (!"SUCCESS".equals(queryResult.getResultCode())) {
            log.error("支付状态查询失败：'ErrCode':'{}', ErrCodeDes':'{}'", queryResult.getErrCode(), queryResult.getErrCodeDes());
            return R.error("支付状态查询失败：" + queryResult.getErrCodeDes());
        }
        */
        String tradeState = queryResult.getTradeState();
        List<String> paidStatus = Arrays.asList("SUCCESS", "REFUND");
        if (paidStatus.contains(tradeState)) {
            return R.error("当前订单状态不支持重新支付,请将订单及提示告知管理员处理：" + payOrder.getOutTradeNo());
        }

        List<String> closeStatus = Arrays.asList("CLOSED", "REVOKED");
        if (closeStatus.contains(tradeState)) {
            return R.ok();
        }
        WxPayOrderCloseRequest request = new WxPayOrderCloseRequest();
        request.setOutTradeNo(payOrder.getOutTradeNo());
        // WxPayOrderCloseResult closeResult = null;
        try {
            wxPayService.closeOrderV3(payOrder.getOutTradeNo());
            // closeResult = wxPayService.closeOrder(payOrder.getOutTradeNo());
        } catch (WxPayException e) {
            log.error("上一次支付取消失败：'ErrCode':'{}', ErrCodeDes':'{}'", e.getErrCode(), e.getErrCodeDes());
            return R.error("上一次支付取消失败：" + e.getErrCodeDes());
        }
        /*
        if (!"SUCCESS".equals(closeResult.getReturnCode())) {
            log.error("上一次支付取消失败：{}", closeResult.getReturnMsg());
            return R.error("上一次支付取消失败：" + closeResult.getReturnMsg());
        }
        if (!"SUCCESS".equals(closeResult.getResultCode())) {
            log.error("上一次支付取消失败：'ErrCode':'{}', ErrCodeDes':'{}'", closeResult.getErrCode(), closeResult.getErrCodeDes());
            return R.error("上一次支付取消失败：" + closeResult.getErrCodeDes());
        }
        */
        return R.ok();
    }


    // 支付回调
    public PayOrder payNotify(PayOrder payOrder, WxPayNotifyV3Result.DecryptNotifyResult notifyResult) {
        String status = payOrder.getPayStatus();
        if (PayStatus.PAID.name().equals(status) || PayStatus.FINISHED.name().equals(status)) {
            String msg = "order is payed @ " + DateUtil.format(payOrder.getPayTime(), "yyyy-MM-dd HH:mm:ss");
            log.warn(msg);
            return null;
        }

        Integer totalFee = notifyResult.getAmount().getTotal();
        BigDecimal paymentAmount = payOrder.getPaymentAmount();
        if (!totalFee.equals(paymentAmount.multiply(new BigDecimal("100")).intValue())) {
            String msg = StrFormatter.format("订单 {} 交易流水号 {} 需支付金额 {}, 支付成功金额： {}, 异常支付，不处理！",
                payOrder.getOutTradeNo(),
                notifyResult.getTransactionId(),
                paymentAmount,
                totalFee);
            throw ValidationException.of(msg);
        }

        // 微信支付返回的信息
        payOrder.setAppid(notifyResult.getAppid());
        payOrder.setMchId(notifyResult.getMchid());

        WxPayNotifyV3Result.SceneInfo sceneInfo = notifyResult.getSceneInfo();
        payOrder.setDeviceInfo(sceneInfo == null ? null : sceneInfo.getDeviceId());

        // 官方文档没有此值
        payOrder.setTradeType(notifyResult.getTradeType());
        payOrder.setBankType(notifyResult.getBankType());
        payOrder.setPayFlowNo(notifyResult.getTransactionId());

        /*
        payOrder.setNonceStr(notifyResult.getNonceStr());
        payOrder.setSign(notifyResult.getSign());
        payOrder.setSignType(notifyResult.getSignType());
        payOrder.setOpenId(notifyResult.getOpenid());
        payOrder.setTotalFee(notifyResult.getTotalFee() == null ? BigDecimal.ZERO : new BigDecimal(notifyResult.getTotalFee()).divide(new BigDecimal("100")));
        payOrder.setSettlementTotalFee(notifyResult.getSettlementTotalFee() == null ? BigDecimal.ZERO : new BigDecimal(notifyResult.getSettlementTotalFee()).divide(new BigDecimal("100")));
        payOrder.setFeeType(notifyResult.getFeeType());
        payOrder.setCashFee(notifyResult.getCashFee() == null ? BigDecimal.ZERO : new BigDecimal(notifyResult.getCashFee()).divide(new BigDecimal("100")));
        payOrder.setCashFeeType(notifyResult.getCashFeeType());
        payOrder.setCouponFee(notifyResult.getCouponFee() == null ? BigDecimal.ZERO : new BigDecimal(notifyResult.getCouponFee()).divide(new BigDecimal("100")));
        payOrder.setCouponCount(notifyResult.getCouponCount());
        List<WxPayOrderNotifyCoupon> couponList = notifyResult.getCouponList();
        payOrder.setCoupon(CollectionUtils.isEmpty(couponList) ? null : JSONArray.toJSONString(couponList));
        payOrder.setIsSubscribe("Y".equals(notifyResult.getIsSubscribe()) ? 1 : 0);
        */

        String successTime = notifyResult.getSuccessTime();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
        Date payTime = null;
        try {
            successTime = successTime.replaceAll("([+-]\\d\\d):\\d\\d$", "$100");
            payTime = sdf.parse(successTime);
        } catch (ParseException e) {
            log.error(e.getMessage(), e);
        }
        payOrder.setPayStatus(PayStatus.PAID.name());
        payOrder.setPayTime(payTime == null ? new Date() : payTime);

        if (payNoticeSpi != null) {
            payNoticeSpi.payidNotice(payOrder);
        }

        return payOrder;
    }

    /*
    // 发起退款
    public R wxTradeRefund(PayOrder payOrder, OmsOrderItem omsOrderItem) {
        R result = new Result();
        if (StringUtils.isBlank(omsOrderItem.getRefundeReason())) {
            omsOrderItem.setRefundeReason("用户申请退款");
        }

        // TODO 最多可退多少，应当与子单做比较
        BigDecimal paymentAmount = payOrder.getPaymentAmount();
        BigDecimal refundeAmount = omsOrderItem.getRefundeAmount();
        if (refundeAmount.compareTo(paymentAmount) > 0) {
            return R.warn("最多可退: " + paymentAmount.toString() + " 元");
        }

        PayWxpayConfig config = getWxpayConfig(payOrder.getTenantCode());
        WxPayService wxService = getWxService(config);
        String payFlowNo = payOrder.getPayFlowNo();

        WxPayRefundRequest request = new WxPayRefundRequest();
        request.setTransactionId(payFlowNo);
        request.setOutTradeNo(payOrder.getOutTradeNo());
        // 只退子单。若子单分多次退款，需要再扩展
        request.setOutRefundNo(omsOrderItem.getOrderItemNo());
        request.setTotalFee(paymentAmount.multiply(new BigDecimal("100")).intValue());
        request.setRefundFee(refundeAmount.multiply(new BigDecimal("100")).intValue());
        request.setRefundDesc(omsOrderItem.getRefundeReason());

        String notifyUrl = config.getRefundNotifyUrl();
        notifyUrl = notifyUrl.replace("{tenantCode}", payOrder.getTenantCode()+"");
        notifyUrl = notifyUrl.replace("{appid}", wxService.getConfig().getAppId());
        request.setNotifyUrl(notifyUrl);

        WxPayRefundResult wxResult = null;
        try {
            wxResult = wxService.refund(request);
        } catch (WxPayException e) {
            if ("FAIL".equals(e.getReturnCode())) {
                log.error(payFlowNo + "退款发起失败，" + e.getReturnMsg());
                return R.error(payFlowNo + "退款发起失败，" + e.getReturnMsg());
            }

            if (!"SUCCESS".equals(e.getResultCode())) {
                log.error(payFlowNo + "退款发起失败，" + e.getErrCodeDes());
                return R.error(payFlowNo + "退款发起失败，" + e.getErrCodeDes());
            }

            log.error("{} 退款失败：{}", payFlowNo, e.getReturnMsg());
            throw ValidationException.of(e.getReturnMsg());
        }

        if (wxResult == null) {
            return result.setError("系统错误，微信支付发起退款失败");
        }

        if (!"SUCCESS".equals(wxResult.getReturnCode())) {
            return result.setError("微信支付发起退款失败：" + wxResult.getReturnMsg());
        }
        if (!"SUCCESS".equals(wxResult.getResultCode())) {
            return result.setError("微信支付发起退款失败：'ErrCode':'" + wxResult.getErrCode() + "', ErrCodeDes':'" + wxResult.getErrCodeDes() + "'");
        }

        return result.setOk();
    }
    */

    // 发起退款
    public String wxTradeRefund(PayOrder payOrder, String refundeReason) {
        if (refundeReason == null) {
            refundeReason = "发起退款";
        }

        BigDecimal paymentAmount = payOrder.getPaymentAmount();

        PayWxpayConfig config = wxpayClientCache.getConfig(payOrder.getTenantCode());
        WxPayService wxPayService = wxpayClientCache.getClient(payOrder.getTenantCode());
        String payFlowNo = payOrder.getPayFlowNo();

        WxPayRefundV3Request request = new WxPayRefundV3Request();
        request.setTransactionId(payFlowNo);
        request.setOutTradeNo(payOrder.getOutTradeNo());
        // 退整单
        request.setOutRefundNo(payOrder.getOrderNo());

        WxPayRefundV3Request.Amount amount = new WxPayRefundV3Request.Amount();
        amount.setTotal(paymentAmount.multiply(new BigDecimal("100")).intValue());
        amount.setCurrency("CNY");
        amount.setRefund(amount.getTotal());

        request.setAmount(amount);
        request.setReason(refundeReason);

        String notifyUrl = config.getRefundNotifyUrl();
        notifyUrl = notifyUrl.replace("{tenantCode}", payOrder.getTenantCode());
        notifyUrl = notifyUrl.replace("{appid}", wxPayService.getConfig().getAppId());
        request.setNotifyUrl(notifyUrl);

        WxPayRefundV3Result wxResult;
        try {
            wxResult = wxPayService.refundV3(request);
        } catch (WxPayException e) {
            if ("FAIL".equals(e.getReturnCode())) {
                log.error(payFlowNo + "退款发起失败，" + e.getReturnMsg());
                throw ValidationException.of(payFlowNo + "退款发起失败，" + e.getReturnMsg());
            }

            if (!"SUCCESS".equals(e.getResultCode())) {
                log.error(payFlowNo + "退款发起失败，" + e.getErrCodeDes());
                throw ValidationException.of(payFlowNo + "退款发起失败，" + e.getErrCodeDes());
            }

            log.error("{} 退款失败：{}", payFlowNo, e.getReturnMsg());
            throw ValidationException.of(e.getReturnMsg());
        }

        if (wxResult == null) {
            throw ValidationException.of("系统错误，微信支付发起退款失败");
        }

        String status = wxResult.getStatus();

        if ("PROCESSING".equals(status)) {
            return "退款处理中，请等待最终结果！";
        }
        if ("SUCCESS".equals(status)) {
            return "微信支付发起退款成功！";
        }

        log.error("{} 退款失败：{}", payFlowNo, wxResult);
        throw ValidationException.of("微信支付发起退款失败!");
    }


    // 【异步调用】退款回调
    public String wxRefundNotify(WxPayRefundNotifyV3Result refundNotifyResult) {
        WxPayRefundNotifyV3Result.DecryptNotifyResult result = refundNotifyResult.getResult();

        String returnCode = result.getRefundStatus();

        if ("FAIL".equals(returnCode)) {
            log.info("----------> 微信支付退款失败，{}", refundNotifyResult);
            return WxPayNotifyResponse.fail("失败");
        }

        if ("SUCCESS".equals(returnCode)) {
            String outTradeNo = result.getOutTradeNo();
            log.info("----------> 微信支付退款成功，{}", result);
            if (StringUtils.isBlank(outTradeNo)) {
                throw ValidationException.of("outTradeNo 为空，无法处理退款");
            }

            PayOrder payOrder = new PayOrder();
            payOrder.setOutTradeNo(outTradeNo);
            payOrder = payOrderService.selectOneByEntity(payOrder);
            if (payOrder == null) {
                throw ValidationException.of("支付订单 {} 不存在，无法处理退款", outTradeNo);
            }

            /*
            // TODO 若分批退款，需要切分 outRefundNo
            OmsOrderItem omsOrderItem = new OmsOrderItem();
            omsOrderItem.setOrderItemNo(outRefundNo);
            omsOrderItem = omsOrderItemService.get(omsOrderItem);
            if (omsOrderItem == null) {
                return WxPayNotifyResponse.fail("退款订单 " + outRefundNo + " 不存在，无法处理退款");
            }

            OmsOrder omsOrder = new OmsOrder();
            omsOrder.setOrderNo(omsOrderItem.getOrderNo());
            omsOrder = omsOrderService.get(omsOrder);
            if (omsOrder == null) {
                return WxPayNotifyResponse.fail("退款订单 " + omsOrderItem.getOrderNo() + " 不存在，无法处理退款");
            }

            // 非拼团订单才考虑 REFUNDING
            if (omsOrder.getOrderGroupId() == null && !"REFUNDING".equals(omsOrderItem.getOrderStatus())) {
                return WxPayNotifyResponse.fail("当前订单不是退款中状态，无法完成退款");
            }
            // 若订单是订金订单，并且尾款未支付, 在订金产生退款的时候, 需要把尾款取消掉。
            if ("DEPOSIT".equals(omsOrderItem.getPriceType())) {
                log.info("订金订单对应的尾款订单：{}, 状态： {}", omsOrder.getOrderNo(), omsOrder.getOrderStatus());
                if ("NEW".equals(omsOrder.getOrderStatus()) || "PAYING".equals(omsOrder.getOrderStatus()) || "PAYERROR".equals(omsOrder.getOrderStatus())) {
                    log.info("订金订单对应的尾款订单-取消订单：{}, 状态： {}", omsOrder.getOrderNo(), omsOrder.getOrderStatus());
                    omsOrder.setOrderStatus("SYSTEM_CANCEL");
                    omsOrderService.updateByIdSelective(omsOrder);
                    OmsOrderItem param2 = new OmsOrderItem();
                    param2.setOrderNo(omsOrder.getOrderNo());
                    List<OmsOrderItem> items = omsOrderItemService.list(param2);
                    if (CollectionUtil.isNotEmpty(items)) {
                        for (OmsOrderItem item : items) {
                            item.setOrderStatus("SYSTEM_CANCEL");
                            omsOrderItemService.updateByIdSelective(item);
                        }
                    }
                }
            }

            omsOrderItem.setOrderStatus("REFUNDED");
            omsOrderItem.setRefundeTime(new Date());
            omsOrderItemService.updateByIdSelective(omsOrderItem);
            */

            // 退款流水
            payOrder.setPayStatus("REFUNDED");

            if (payNoticeSpi != null) {
                payNoticeSpi.refoundNotice(payOrder);
            }


            payOrderService.update(payOrder);
            // payFlowService.logPayFlow(payOrder);
            return WxPayNotifyResponse.success("成功");
        }

        log.info("----------> 微信支付未知的回调，{}", refundNotifyResult);
        return WxPayNotifyResponse.fail("未知的回调");
    }





    public static SignatureHeader getRequestHeader(HttpServletRequest request) {
        if (request == null) {
            throw ValidationException.of("request 不能为空");
        }
        // 获取通知签名
        String signature = request.getHeader("Wechatpay-Signature");
        String nonce = request.getHeader("Wechatpay-Nonce");
        String serial = request.getHeader("Wechatpay-Serial");
        String timestamp = request.getHeader("Wechatpay-Timestamp");

        SignatureHeader signatureHeader = new SignatureHeader();
        signatureHeader.setSignature(signature);
        signatureHeader.setNonce(nonce);
        signatureHeader.setSerial(serial);
        signatureHeader.setTimeStamp(timestamp);
        return signatureHeader;
    }

}
