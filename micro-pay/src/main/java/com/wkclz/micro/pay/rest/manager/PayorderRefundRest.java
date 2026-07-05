package com.wkclz.micro.pay.rest.manager;

import com.wkclz.core.base.R;
import com.wkclz.micro.pay.bean.entity.PayOrder;
import com.wkclz.micro.pay.bean.req.PayOrderRefundReq;
import com.wkclz.micro.pay.rest.Route;
import com.wkclz.micro.pay.service.ShopOrderService;
import com.wkclz.tool.utils.BeanUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "4.退款管理", description = "支付订单退款管理接口")
@RestController
@RequestMapping(Route.PREFIX)
@Validated
public class PayorderRefundRest {

    @Resource
    private ShopOrderService shopOrderService;

    @Operation(summary = "1.退款-申请", description = "申请支付订单退款（subOrderNo 为空表示总单退款，非空表示子单退款）")
    @PostMapping(Route.PAYORDER_REFUND)
    public R<String> refund(@Valid @RequestBody PayOrderRefundReq req) {
        PayOrder entity = BeanUtil.cp(req, PayOrder.class);
        String reason = req.getReason() != null ? req.getReason() : "商家退款";
        String s = shopOrderService.managerPayRefund(entity, req.getRefundAmount(), req.getRefundNo(), reason, req.getSubOrderNo());
        return R.ok(s);
    }
}
