package com.wkclz.micro.pay.dao;

import com.wkclz.micro.pay.pojo.dto.PayOrderDto;
import com.wkclz.micro.pay.pojo.entity.PayOrder;
import com.wkclz.mybatis.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * Description Create by shrimp-gen
 * @author wangkaicun
 * @table pay_order (支付-订单) DAO 接口，代码重新生成不覆盖
 */

@Mapper
public interface PayOrderMapper extends BaseMapper<PayOrder> {

    List<PayOrder> getActivePayOrder(PayOrder entity);

    PayOrder getOrderStatus(PayOrder param);

    PayOrder getPayOrderByOutTradeNo(@Param("outTradeNo") String outTradeNo);

    List<PayOrder> getPayingOrders();

    List<PayOrder> getTimeoutPayingOrders(PayOrderDto param);

}



