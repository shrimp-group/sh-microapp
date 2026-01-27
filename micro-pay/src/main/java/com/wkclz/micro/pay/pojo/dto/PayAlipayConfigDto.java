package com.wkclz.micro.pay.pojo.dto;

import com.wkclz.micro.pay.pojo.entity.PayAlipayConfig;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Description Create by shrimp-gen
 * @author wangkaicun
 * @table pay_alipay_config (支付-支付宝配置) 数据库实例扩展，代码重新生成不覆盖
 */

@Data
@EqualsAndHashCode(callSuper = false)
public class PayAlipayConfigDto extends PayAlipayConfig {


    private String tenantName;



    /**
     * entity 转 Dto
     * @param source
     * @return
     */
    public static PayAlipayConfigDto copy(PayAlipayConfig source) {
        PayAlipayConfigDto target = new PayAlipayConfigDto();
        PayAlipayConfig.copy(source, target);
        return target;
    }
}

