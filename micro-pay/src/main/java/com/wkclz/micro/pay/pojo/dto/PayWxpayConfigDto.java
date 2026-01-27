package com.wkclz.micro.pay.pojo.dto;

import com.wkclz.micro.pay.pojo.entity.PayWxpayConfig;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Description Create by shrimp-gen
 * @author wangkaicun
 * @table pay_wxpay_config (支付-微信支付配置) 数据库实例扩展，代码重新生成不覆盖
 */

@Data
@EqualsAndHashCode(callSuper = false)
public class PayWxpayConfigDto extends PayWxpayConfig {


    private String tenantName;



    /**
     * entity 转 Dto
     * @param source
     * @return
     */
    public static PayWxpayConfigDto copy(PayWxpayConfig source) {
        PayWxpayConfigDto target = new PayWxpayConfigDto();
        PayWxpayConfig.copy(source, target);
        return target;
    }
}

