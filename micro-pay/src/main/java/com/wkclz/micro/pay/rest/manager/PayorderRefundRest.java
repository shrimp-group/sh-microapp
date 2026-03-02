package com.wkclz.micro.pay.rest.manager;

import com.wkclz.core.base.R;
import com.wkclz.micro.pay.pojo.entity.PayOrder;
import com.wkclz.micro.pay.rest.Route;
import com.wkclz.micro.pay.service.OrderService;
import jakarta.annotation.Resource;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author shrimp
 */

@RestController
@RequestMapping(Route.PREFIX)
public class PayorderRefundRest {


    @Resource
    private OrderService orderService;

    @PostMapping(Route.PAYORDER_REFUND)
    public R refund(@RequestBody PayOrder entity) {
        Assert.notNull(entity.getOrderNo(), "orderNo 不能为空");
        String s = orderService.managerPayRefund(entity, "商家退款");
        return R.ok(s);
    }

}
