package com.wkclz.micro.pay.schedule;

import cn.hutool.core.date.LocalDateTimeUtil;
import cn.hutool.core.text.StrFormatter;
import com.github.binarywang.wxpay.bean.request.WxPayOrderQueryV3Request;
import com.github.binarywang.wxpay.bean.result.WxPayOrderQueryV3Result;
import com.github.binarywang.wxpay.constant.WxPayConstants;
import com.github.binarywang.wxpay.exception.WxPayException;
import com.github.binarywang.wxpay.service.WxPayService;
import com.wkclz.core.exception.ValidationException;
import com.wkclz.micro.pay.bean.dto.OrderPayResult;
import com.wkclz.micro.pay.bean.entity.PayOrder;
import com.wkclz.micro.pay.bean.enums.PayStatus;
import com.wkclz.micro.pay.cache.WxpayClientCache;
import com.wkclz.micro.pay.config.PayConfig;
import com.wkclz.micro.pay.mapper.PayOrderMapper;
import com.wkclz.micro.pay.spi.PayOrderSpi;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
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
    private PayOrderSpi payOrderSpi;
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
        if (payOrderSpi == null) {
            throw ValidationException.of("订单交互服务未配置");
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

                String successTime = result.getSuccessTime().replaceAll("([+-]\\d\\d):\\d\\d$", "$100");
                LocalDateTime payTime = LocalDateTimeUtil.parse(successTime, "yyyy-MM-dd'T'HH:mm:ssZ");

                order.setPayStatus(PayStatus.PAID.name());
                order.setPayTime(payTime);
                payOrderMapper.updateByIdSelective(order);

                try {
                    OrderPayResult payResult = new OrderPayResult();
                    payResult.setOrderNo(order.getOrderNo());
                    payResult.setPayMethod(order.getPayMethod());
                    payResult.setPayFlowNo(order.getPayFlowNo());
                    payResult.setPayTime(order.getPayTime());
                    payOrderSpi.updateOrderToPaid(payResult);
                } catch (Exception e) {
                    log.error("微信支付状态同步，更新订单状态失败, orderNo: {}", order.getOrderNo(), e);
                }

                log.info("订单已支付成功: {}", outTradeNo);
            } catch (WxPayException e) {
                throw new RuntimeException(e);
            }
        }
    }

}
