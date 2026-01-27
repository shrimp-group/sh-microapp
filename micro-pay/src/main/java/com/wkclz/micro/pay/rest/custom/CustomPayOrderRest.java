package com.wkclz.micro.pay.rest.custom;

import com.wkclz.core.base.R;
import com.wkclz.core.enums.EnvType;
import com.wkclz.micro.pay.pojo.entity.PayOrder;
import com.wkclz.micro.pay.pojo.enums.PayMethod;
import com.wkclz.micro.pay.rest.Route;
import com.wkclz.micro.pay.service.PayOrderService;
import com.wkclz.spring.config.Sys;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author shrimp
 */
@RestController
public class CustomPayOrderRest {


    @Autowired
    private PayOrderService payOrderService;


    @PostMapping(Route.COMMON_PAYORDER_MOCK_PAY)
    public R commonPayorderMockPay(@RequestBody PayOrder payOrder) {
        if (Sys.getCurrentEnv() == EnvType.PROD) {
            return R.error("非法的支付方式!");
        }
        Assert.notNull(payOrder.getOrderNo(), "orderNo 不能为空");
        Assert.notNull(payOrder.getPayMethod(), "payMethod 不能为空");

        if (!PayMethod.MOCK_PAY.name().equals(payOrder.getPayMethod())) {
            return R.error("非法的支付方式!");
        }
        payOrderService.mockPay(payOrder);
        return R.ok(payOrder);
    }

    @GetMapping(Route.COMMON_PAYORDER_STATUS)
    public R commonPayorderStatus(PayOrder payOrder) {
        Assert.notNull(payOrder.getOrderNo(), "orderNo 不能为空");
        payOrder = payOrderService.getPayOrderStatus2Custom(payOrder);
        return R.ok(payOrder);
    }


}
