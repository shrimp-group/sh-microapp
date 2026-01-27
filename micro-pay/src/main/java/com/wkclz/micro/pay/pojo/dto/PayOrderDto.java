package com.wkclz.micro.pay.pojo.dto;

import com.wkclz.micro.pay.pojo.entity.PayOrder;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Map;

/**
 * Description Create by shrimp-gen
 * @author wangkaicun
 * @table pay_order (支付-订单) 数据库实例扩展，代码重新生成不覆盖
 */

@Data
@EqualsAndHashCode(callSuper = false)
public class PayOrderDto extends PayOrder {

    // 支付宝支付时的返回，便于回调
    private String aliPayBody;
    private String wxpayUrl;

    private String prepayId;
    private Map<String, Object> jsapiResult;


    private Integer timeoutMinute;


    /**
     * entity 转 Dto
     * @param source
     * @return
     */
    public static PayOrderDto copy(PayOrder source) {
        PayOrderDto target = new PayOrderDto();
        PayOrder.copy(source, target);
        return target;
    }
}

