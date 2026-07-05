package com.wkclz.micro.pay.rest.custom;

import com.wkclz.core.base.R;
import com.wkclz.core.enums.EnvType;
import com.wkclz.micro.pay.bean.entity.PayOrder;
import com.wkclz.micro.pay.bean.enums.PayMethod;
import com.wkclz.micro.pay.bean.req.PayOrderMockPayReq;
import com.wkclz.micro.pay.bean.req.PayOrderReq;
import com.wkclz.micro.pay.bean.req.PayOrderStatusReq;
import com.wkclz.micro.pay.bean.dto.PayOrderDto;
import com.wkclz.micro.pay.bean.resp.PayOrderMockPayResp;
import com.wkclz.micro.pay.bean.resp.PayOrderPayResp;
import com.wkclz.micro.pay.bean.resp.PayOrderStatusResp;
import com.wkclz.micro.pay.rest.Route;
import com.wkclz.micro.pay.service.PayOrderService;
import com.wkclz.micro.pay.service.ShopOrderService;
import com.wkclz.spring.config.Sys;
import com.wkclz.tool.utils.BeanUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Tag(name = "3.支付订单", description = "用户端支付订单操作接口")
@Slf4j
@RestController
@RequestMapping(Route.PREFIX)
@Validated
public class CustomPayOrderRest {

    @Autowired
    private PayOrderService payOrderService;
    @Autowired
    private ShopOrderService shopOrderService;


    @Operation(summary = "1.支付订单-模拟支付", description = "模拟支付（生产环境禁用）")
    @PostMapping(Route.COMMON_PAYORDER_PAY_MOCK)
    public R<PayOrderMockPayResp> commonPayorderPayMock(@Valid @RequestBody PayOrderMockPayReq req) {
        if (Sys.getCurrentEnv() == EnvType.PROD) {
            return R.error("非法的支付方式!");
        }
        if (!PayMethod.MOCK_PAY.name().equals(req.getPayMethod())) {
            return R.error("非法的支付方式!");
        }

        // 委托 service 层处理完整模拟支付流程（订单校验、创建 pay_order、SPI 调用、模拟回调）
        PayOrderDto dto = shopOrderService.mockPayWithOrderInfo(req);

        PayOrderMockPayResp resp = new PayOrderMockPayResp();
        resp.setOrderNo(dto.getOrderNo());
        resp.setOutTradeNo(dto.getOutTradeNo());
        resp.setPayStatus(dto.getPayStatus());
        resp.setPayMethod(dto.getPayMethod());
        resp.setPayFlowNo(dto.getPayFlowNo());
        resp.setPayTime(dto.getPayTime());
        return R.ok(resp);
    }



    @Operation(summary = "2.支付订单-发起支付", description = "通过订单号发起支付，订单信息通过 SPI 获取")
    @PostMapping(Route.COMMON_PAYORDER_PAY)
    public R<PayOrderPayResp> commonPayorderPay(@RequestBody PayOrderReq req, HttpServletRequest httpReq, HttpServletResponse httpRep) {
        PayOrderDto dto = shopOrderService.payWithOrderInfo(req, httpReq, httpRep);

        PayOrderPayResp resp = new PayOrderPayResp();
        resp.setOrderNo(dto.getOrderNo());
        resp.setOutTradeNo(dto.getOutTradeNo());
        resp.setPayStatus(dto.getPayStatus());
        resp.setPayMethod(dto.getPayMethod());
        resp.setAliPayBody(dto.getAliPayBody());
        resp.setPrepayId(dto.getPrepayId());
        resp.setJsapiResult(dto.getJsapiResult());
        return R.ok(resp);
    }

    @Operation(summary = "3.支付订单-状态查询", description = "查询支付订单状态")
    @GetMapping(Route.COMMON_PAYORDER_STATUS)
    public R<PayOrderStatusResp> commonPayorderStatus(@Valid PayOrderStatusReq req) {
        PayOrder payOrder = BeanUtil.cp(req, PayOrder.class);
        payOrder = payOrderService.getPayOrderStatus2Custom(payOrder);
        PayOrderStatusResp resp = BeanUtil.cp(payOrder, PayOrderStatusResp.class);
        return R.ok(resp);
    }
}
