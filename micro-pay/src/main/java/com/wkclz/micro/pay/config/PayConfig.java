package com.wkclz.micro.pay.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

/**
 * @author shrimp
 */
@Data
@Configuration
public class PayConfig {

    @Value("${pay.wxpay.pay-status-sync.enable:1}")
    private Integer payStatusSyncEnable;

    @Value("${pay.pay-timeout-cancel.enable:1}")
    private Integer payTimeoutCancelEnable;

    @Value("${pay.pay-timeout-cancel.minute:1440}")
    private int payTimeoutCancelMinute;

    /** 退款时是否退还积分（1=启用，0=关闭，默认 1）；仅控制退款环节，不影响支付失败补偿与未支付取消 */
    @Value("${pay.points-refund-on-refund.enable:1}")
    private Integer pointsRefundOnRefundEnable;


}
