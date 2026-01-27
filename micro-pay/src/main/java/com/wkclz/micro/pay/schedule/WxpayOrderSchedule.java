package com.wkclz.micro.pay.schedule;

import cn.hutool.core.text.StrFormatter;
import com.github.binarywang.wxpay.bean.request.WxPayOrderQueryV3Request;
import com.github.binarywang.wxpay.bean.result.WxPayOrderQueryV3Result;
import com.github.binarywang.wxpay.constant.WxPayConstants;
import com.github.binarywang.wxpay.exception.WxPayException;
import com.github.binarywang.wxpay.service.WxPayService;
import com.wkclz.core.exception.ValidationException;
import com.wkclz.micro.pay.cache.WxpayClientCache;
import com.wkclz.micro.pay.config.PayConfig;
import com.wkclz.micro.pay.dao.PayOrderMapper;
import com.wkclz.micro.pay.pojo.entity.PayOrder;
import com.wkclz.micro.pay.pojo.enums.PayStatus;
import com.wkclz.micro.pay.spi.PayNoticeSpi;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * @author shrimp
 */
@Slf4j
@Component
public class WxpayOrderSchedule {

    @Resource
    private PayConfig payConfig;
    @Autowired(required = false)
    private PayNoticeSpi payNoticeSpi;
    @Resource
    private PayOrderMapper payOrderMapper;
    @Resource
    private WxpayClientCache wxpayClientCache;

    @Scheduled(fixedDelay = 300_000, initialDelay = 38_000)
    public void wxOrderPayStatusSync() {
        Integer enable = payConfig.getPayStatusSyncEnable();
        if (enable == null || enable != 1) {
            return;
        }
        List<PayOrder> orders = payOrderMapper.getPayingOrders();
        if (CollectionUtils.isEmpty(orders)) {
            return;
        }

        for (PayOrder order : orders) {
            String outTradeNo = order.getOutTradeNo();
            WxPayService client = wxpayClientCache.getClient(order.getTenantCode());
            WxPayOrderQueryV3Request request = new WxPayOrderQueryV3Request();
            request.setOutTradeNo(outTradeNo);
            request.setMchid(order.getMchId());

            try {
                WxPayOrderQueryV3Result result = client.queryOrderV3(request);

                if (!WxPayConstants.WxpayTradeStatus.SUCCESS.equals(result.getTradeState())) {
                    log.info("定时任务: 支付仍未成功: {}, {}", outTradeNo, result.getTradeState());
                    continue;
                }

                Integer totalFee = result.getAmount().getTotal();
                BigDecimal paymentAmount = order.getPaymentAmount();
                if (!totalFee.equals(paymentAmount.multiply(new BigDecimal("100")).intValue())) {
                    String msg = StrFormatter.format("订单 {} 交易流水号 {} 需支付金额 {}, 支付成功金额： {}, 异常支付，不处理！",
                        order.getOutTradeNo(),
                        result.getTransactionId(),
                        paymentAmount,
                        totalFee);
                    throw ValidationException.of(msg);
                }

                WxPayOrderQueryV3Result.SceneInfo sceneInfo = result.getSceneInfo();
                order.setDeviceInfo(sceneInfo == null ? null : sceneInfo.getDeviceId());
                order.setTradeType(result.getTradeType());
                order.setBankType(result.getBankType());
                order.setPayFlowNo(result.getTransactionId());

                String successTime = result.getSuccessTime();
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
                Date payTime = null;
                try {
                    successTime = successTime.replaceAll("([+-]\\d\\d):\\d\\d$", "$100");
                    payTime = sdf.parse(successTime);
                } catch (ParseException e) {
                    log.error(e.getMessage(), e);
                }
                order.setPayStatus(PayStatus.PAID.name());
                order.setPayTime(payTime == null ? new Date() : payTime);
                payOrderMapper.updateByIdSelective(order);

                if (payNoticeSpi != null) {
                    payNoticeSpi.payidNotice(order);
                }

                log.info("订单已支付成功: {}", outTradeNo);
            } catch (WxPayException e) {
                throw new RuntimeException(e);
            }
        }
    }

}
