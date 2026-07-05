package com.wkclz.micro.pay.spi;

import com.wkclz.micro.pay.bean.dto.OrderInfoForPay;
import com.wkclz.micro.pay.bean.dto.OrderPayResult;

/**
 * 支付-订单交互 SPI 接口
 * 供订单模块实现，使支付模块能在不依赖订单模块具体实现的情况下
 * 获取订单支付所需信息，以及在支付流程各阶段更新订单状态
 * 合并了原 OrderInfoSpi 和 PayNoticeSpi，每个支付事件只需调用一次
 */
public interface PayOrderSpi {

    /**
     * 查询订单支付信息
     *
     * @param orderNo 订单号
     * @return 订单支付信息，订单不存在时返回null
     */
    OrderInfoForPay getOrderInfoForPay(String orderNo);

    /**
     * 更新订单状态为支付中
     *
     * @param orderNo 订单号
     */
    void updateOrderToPaying(String orderNo);

    /**
     * 更新订单支付成功
     *
     * @param payResult 支付结果信息
     */
    void updateOrderToPaid(OrderPayResult payResult);

    /**
     * 更新订单退款成功
     *
     * @param orderNo 订单号
     */
    void updateOrderToRefunded(String orderNo);

    /**
     * 更新订单支付超时
     *
     * @param orderNo 订单号
     */
    void updateOrderToTimeout(String orderNo);
}
