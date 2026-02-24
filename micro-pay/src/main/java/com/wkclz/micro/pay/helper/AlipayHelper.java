package com.wkclz.micro.pay.helper;

import cn.hutool.core.date.DateUtil;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.domain.AlipayTradeWapPayModel;
import com.alipay.api.internal.util.AlipaySignature;
import com.alipay.api.request.AlipayTradeCloseRequest;
import com.alipay.api.request.AlipayTradePagePayRequest;
import com.alipay.api.request.AlipayTradeWapPayRequest;
import com.alipay.api.response.AlipayTradeCloseResponse;
import com.alipay.api.response.AlipayTradePagePayResponse;
import com.alipay.api.response.AlipayTradeWapPayResponse;
import com.wkclz.core.base.R;
import com.wkclz.core.exception.ValidationException;
import com.wkclz.micro.pay.cache.AlipayClientCache;
import com.wkclz.micro.pay.pojo.dto.PayOrderDto;
import com.wkclz.micro.pay.pojo.entity.PayAlipayConfig;
import com.wkclz.micro.pay.pojo.entity.PayOrder;
import com.wkclz.micro.pay.pojo.enums.PayStatus;
import com.wkclz.micro.pay.pojo.enums.TerminalType;
import com.wkclz.micro.pay.pojo.vo.AlipayNotify;
import com.wkclz.web.helper.RequestHelper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

/**
 * @author shrimp
 */

@Slf4j
@Component
public class AlipayHelper {

    @Autowired
    private AlipayClientCache alipayClientCache;


    // 发起支付
    public PayOrderDto pay(PayOrder payOrder, HttpServletRequest req, HttpServletResponse rep) {

        AlipayClient client = alipayClientCache.getClient(payOrder.getTenantCode());
        PayAlipayConfig config = alipayClientCache.getConfig(payOrder.getTenantCode());

        // 支付成功的返回地址
        String frontPortalAnddDomain = RequestHelper.getFrontPortalDomainPort(req);
        String returnUrl = frontPortalAnddDomain + config.getReturnUrl() + "?orderNo=" + payOrder.getOutTradeNo();

        /**
         * 基本信息
         */

        //商户订单号，商户网站订单系统中唯一订单号，必填
        String outTradeNo = payOrder.getOutTradeNo();
        //付款金额，必填
        String totalAmount = payOrder.getTotalAmount().toString();
        //订单名称，必填
        String subject = payOrder.getDetail();
        //商品描述，可空
        String body = payOrder.getBody();
        TerminalType terminalType = TerminalType.valueOf(payOrder.getTerminalType());

        String htmlBody = null;
        // PC 网站支付
        if (terminalType == TerminalType.PC) {

            //设置请求参数
            AlipayTradePagePayRequest alipayRequest = new AlipayTradePagePayRequest();
            alipayRequest.setReturnUrl(returnUrl);
            alipayRequest.setNotifyUrl(config.getNotifyUrl());

            String bizContent = String.format("""
                {
                    "out_trade_no": "%s",
                    "total_amount": "%s",
                    "subject": "%s",
                    "body": "%s",
                    "product_code": "FAST_INSTANT_TRADE_PAY",
                }
                """, outTradeNo, totalAmount, subject, body);


            /**
             * 若想给BizContent增加其他可选请求参数，以增加自定义超时时间参数timeout_express来举例说明
             * 请求参数可查阅【电脑网站支付的API文档-alipay.trade.page.pay-请求参数】章节
                String bizContent = String.format("""
                    {
                        "out_trade_no": "%s",
                        "total_amount": "%s",
                        "subject": "%s",
                        "body": "%s",
                        "timeout_express": "10m",
                        "product_code": "FAST_INSTANT_TRADE_PAY",
                    }
                    """, outTradeNo, totalAmount, subject, body);
             */

            alipayRequest.setBizContent(bizContent);

            log.info("当前支付 APP_ID -----> {}", config.getAppId());
            log.info("订单正在支付 -----> {}", alipayRequest.getBizContent());

            //请求
            AlipayTradePagePayResponse alipayResponse = null;
            try {
                alipayResponse = client.pageExecute(alipayRequest);
            } catch (AlipayApiException e) {
                log.error("AlipayApiException Mag: {}", e.getMessage(), e);
                throw ValidationException.of(e.getErrMsg());
            }
            htmlBody = alipayResponse.getBody();
        }

        // 手机网站支付
        if (terminalType == TerminalType.H5) {

            AlipayTradeWapPayRequest alipayRequest = new AlipayTradeWapPayRequest();

            // 封装请求支付信息
            AlipayTradeWapPayModel model = new AlipayTradeWapPayModel();

            model.setOutTradeNo(outTradeNo);
            model.setSubject(subject);
            model.setTotalAmount(totalAmount);
            model.setBody(body);
            // model.setTimeoutExpress(timeout_express);
            model.setProductCode("QUICK_WAP_WAY");

            alipayRequest.setBizModel(model);
            // 设置异步通知地址
            alipayRequest.setNotifyUrl(config.getNotifyUrl());
            // 设置同步地址
            alipayRequest.setReturnUrl(returnUrl);

            AlipayTradeWapPayResponse alipayResponse = null;
            try {
                alipayResponse = client.pageExecute(alipayRequest);
            } catch (AlipayApiException e) {
                log.error("AlipayApiException Mag: {}", e.getMessage(), e);
            }
            htmlBody = alipayResponse.getBody();
        }

        // 直接将完整的表单html输出到页面
        PayOrderDto payOrderDto = PayOrderDto.copy(payOrder);
        payOrderDto.setAliPayBody(htmlBody);
        return payOrderDto;
    }


    // 支付回调
    @Transactional(rollbackFor = Exception.class)
    public PayOrder payNotify(HttpServletRequest req, HttpServletResponse rep, PayOrder payOrder, AlipayNotify notify) {
        String status = payOrder.getPayStatus();
        if (PayStatus.FINISHED.name().equals(status)) {
            String msg = "order is payed @ " + DateUtil.format(payOrder.getPayTime(), "yyyy-MM-dd HH:mm:ss");
            log.warn(msg);
            return null;
        }

        //——请在这里编写您的程序（以下代码仅作参考）——

        /* 实际验证过程建议商户务必添加以下校验：
        1.需要验证该通知数据中的out_trade_no是否为商户系统中创建的订单号，
        2.判断total_amount是否确实为该订单的实际金额（即商户订单创建时的金额），
        3.校验通知中的seller_id（或者seller_email) 是否为out_trade_no这笔单据的对应的操作方（有的时候，一个商户可能有多个seller_id/seller_email）
        4.验证app_id是否为该商户本身。
        */

        //支付宝交易号
        String tradeNo = notify.getTradeNo();
        //交易状态
        String tradeStatus = notify.getTradeStatus();

        if ("TRADE_FINISHED".equals(tradeStatus)) {
            //判断该笔订单是否在商户网站中已经做过处理
            //如果没有做过处理，根据订单号（out_trade_no）在商户网站的订单系统中查到该笔订单的详细，并执行商户的业务程序
            //如果有做过处理，不执行商户的业务程序
            //注意：
            //退款日期超过可退款期限后（如三个月可退款），支付宝系统发送该交易状态通知
            payOrder.setPayStatus(PayStatus.FINISHED.name());
            return payOrder;
        } else if ("TRADE_SUCCESS".equals(tradeStatus)) {
            //判断该笔订单是否在商户网站中已经做过处理
            //如果没有做过处理，根据订单号（out_trade_no）在商户网站的订单系统中查到该笔订单的详细，并执行商户的业务程序
            //如果有做过处理，不执行商户的业务程序

            //注意：
            //付款完成后，支付宝系统发送该交易状态通知
            String gmtPayment = null;
            Date gmtPaymentTime = null;
            try {
                gmtPayment = new String(req.getParameter("gmt_payment").getBytes(StandardCharsets.ISO_8859_1), StandardCharsets.UTF_8);
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                gmtPaymentTime = sdf.parse(gmtPayment);
            } catch (ParseException e) {
                log.error(e.getMessage(), e);
            }

            payOrder.setPayStatus(PayStatus.PAID.name());
            payOrder.setPayFlowNo(tradeNo);
            payOrder.setPayTime(gmtPaymentTime == null ? new Date() : gmtPaymentTime);
            return payOrder;
        }
        throw ValidationException.of("支付宝支付回调未找到处理逻辑:" + payOrder.getOutTradeNo());
    }


    // 发起支付关闭【未支付的订单，其实是不需要先取消的，此处代码暂不需要写】
    public R payClose(PayOrder payOrder) {
        //设置请求参数
        AlipayTradeCloseRequest alipayRequest = new AlipayTradeCloseRequest();

        String outTradeNo = payOrder.getOutTradeNo();
        String userCode = payOrder.getUserCode();
        alipayRequest.setBizContent(String.format("""
            {
                "out_trade_no": "%s",
                "operator_id": "%s",
            }
            """, outTradeNo, userCode));

        log.info("--------->  订单向支付宝发起取消： {}", alipayRequest.getBizContent());

        AlipayClient client = alipayClientCache.getClient(payOrder.getTenantCode());

        try {
            AlipayTradeCloseResponse response = client.execute(alipayRequest);
            if (!response.isSuccess()) {
                return R.warn(response.getMsg());
            }
            return R.ok(response.getBody());
        } catch (AlipayApiException e) {
            log.error(e.getMessage());
            return R.error(e.getMessage());
        }
    }

    /*

    // 发起退款【暂时只支持全额退款】
    public R tradeRefund(PayOrder payOrder, OmsOrderItem omsOrderItem, UserInfo user) {
        R result = new Result();

        // TODO 最多可退，应当与子单做对比，需要进一步优化
        BigDecimal paymentAmount = payOrder.getPaymentAmount();
        if (omsOrderItem.getRefundeAmount().compareTo(payOrder.getPaymentAmount()) > 0) {
            return R.warn("最多可退: " + paymentAmount.toString() + " 元");
        }

        // 只需要一个参数确认订单即可
        String payFlowNo = payOrder.getPayFlowNo();
        String outTradeNo = payOrder.getOutTradeNo();
        if (StringUtils.isBlank(omsOrderItem.getRefundeReason())) {
            omsOrderItem.setRefundeReason("发起退款");
        }

        //设置请求参数
        AlipayTradeRefundRequest alipayRequest = new AlipayTradeRefundRequest();
        String operatorId = user == null ? "system" : user.getUsername() + "";
        alipayRequest.setBizContent("{" +
            //    "\"out_trade_no\":\""+ orderNo +"\"," +
            "\"trade_no\":\"" + payFlowNo + "\"," +
            "\"refund_amount\":\"" + omsOrderItem.getRefundeAmount() + "\"," +
            "\"refund_reason\":\"" + omsOrderItem.getRefundeReason() + "\"," +
            "\"operator_id\":\"" + operatorId + "\"," +
            "\"out_request_no\":\"" + outTradeNo + "\"" +
            "}");

        log.info("------> 正在发起退款： {}", alipayRequest.getBizContent());

        AlipayClient client = alipayClientCache.getClient(payOrder.getTenantCode());
        AlipayTradeRefundResponse response = null;
        try {
            response = client.execute(alipayRequest);
        } catch (AlipayApiException e) {
            e.printStackTrace();
            log.error("请求出错，接口调用异常 error : {}", e.getMessage());
            return result.setError(e.getMessage());
        }
        if (!response.isSuccess()) {
            return result.setRemind(response.getSubMsg());
        }
        return result.setOk();
    }


    // 整单退
    public R tradeRefund(PayOrder payOrder, UserInfo user) {
        R result = new Result();

        BigDecimal paymentAmount = payOrder.getPaymentAmount();

        // 只需要一个参数确认订单即可
        String payFlowNo = payOrder.getPayFlowNo();
        String outTradeNo = payOrder.getOutTradeNo();

        //设置请求参数
        AlipayTradeRefundRequest alipayRequest = new AlipayTradeRefundRequest();
        String operatorId = user == null ? "system" : user.getUsername() + "";
        alipayRequest.setBizContent("{" +
            //    "\"out_trade_no\":\""+ orderNo +"\"," +
            "\"trade_no\":\"" + payFlowNo + "\"," +
            "\"refund_amount\":\"" + paymentAmount + "\"," +
            "\"refund_reason\":\"拼团失败退款\"," +
            "\"operator_id\":\"" + operatorId + "\"," +
            "\"out_request_no\":\"" + outTradeNo + "\"" +
            "}");

        log.info("------> 正在发起退款： {}", alipayRequest.getBizContent());

        AlipayClient client = alipayClientCache.getClient(payOrder.getTenantCode());
        AlipayTradeRefundResponse response = null;
        try {
            response = client.execute(alipayRequest);
        } catch (AlipayApiException e) {
            e.printStackTrace();
            log.error("请求出错，接口调用异常 error : {}", e.getMessage());
            return result.setError(e.getMessage());
        }
        if (!response.isSuccess()) {
            return result.setRemind(response.getSubMsg());
        }
        return result.setOk();
    }

    */


    public boolean signVerifie(HttpServletResponse rep, Map<String, String> params, String tenantCode) {
        PayAlipayConfig config = alipayClientCache.getConfig(tenantCode);
        // 调用SDK验证签名
        boolean signVerified = false;
        try {
            signVerified = AlipaySignature.rsaCheckV1(params, config.getAlipayPublicKey(), config.getCharset(), config.getSignType());
        } catch (AlipayApiException e) {
            log.error("AlipayApiException Mag: {}", e.getMessage(), e);
        }

        if (!signVerified){
            //验证失败
            log.error(" ------------> check sign fail, AlipayTenantCode: {}", config.getTenantCode());

            //调试用，写文本函数记录程序运行情况是否正常
            String sWord = AlipaySignature.getSignCheckContentV1(params);
            log.error(sWord);
            printBack(rep, "failure");
        }
        return signVerified;

    }

    /**
     * 返回通知
     */
    public static void printBack(HttpServletResponse rep, String body) {
        try {
            rep.setContentType("text/html;charset=" + StandardCharsets.UTF_8.name());
            rep.getWriter().write(body);
            rep.getWriter().flush();
            rep.getWriter().close();
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }
    }


}
