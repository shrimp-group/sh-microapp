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


}
