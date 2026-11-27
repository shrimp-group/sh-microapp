package com.wkclz.micro.pay.service;

import com.wkclz.core.enums.ResultCode;
import com.wkclz.core.exception.ValidationException;
import com.wkclz.core.identity.IdentityContext;
import com.wkclz.micro.pay.bean.entity.PayOrder;
import com.wkclz.micro.pay.mapper.PayOrderMapper;
import com.wkclz.mybatis.service.BaseService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Description Create by shrimp-gen
 * @author wangkaicun
 * @table pay_order (支付-订单) 单表服务类，代码重新生成不覆盖. 只建议完成单表的逻辑，或主表为 pay_order 的逻辑. 其他逻辑放 custom 中
 */

@Slf4j
@Service
public class PayOrderService extends BaseService<PayOrder, PayOrderMapper> {


    public List<PayOrder> getActivePayOrder(PayOrder entity) {
        return mapper.getActivePayOrder(entity);
    }

    public PayOrder getPayOrderStatus2Custom(PayOrder entity) {
        log.info("用户端查询支付订单状态, req={}", entity);
        entity.setUserCode(IdentityContext.getUserCode());

        entity = mapper.getOrderStatus(entity);
        if (entity == null) {
            throw ValidationException.of("您无权查看此订单信息!");
        }
        return entity;
    }

    public PayOrder getPayOrderByOutTradeNo(String outTradeNo) {
        if (StringUtils.isBlank(outTradeNo)) {
            throw ValidationException.of("outTradeNo 不能为空");
        }
        return mapper.getPayOrderByOutTradeNo(outTradeNo);
    }

    public PayOrder create(PayOrder entity) {
        log.info("创建支付订单, req={}", entity);
        // pay_order 无唯一业务键约束（outTradeNo 由 ShopOrderService 生成逻辑保证唯一，同一 orderNo 允许多条历史支付单），无需重复校验
        mapper.insert(entity);
        return entity;
    }

    public PayOrder update(PayOrder entity) {
        log.info("更新支付订单(状态流转), req={}", entity);
        PayOrder oldEntity = selectById(entity.getId());
        if (oldEntity == null) {
            throw ValidationException.of(ResultCode.RECORD_NOT_EXIST);
        }
        PayOrder.copyIfNotNull(entity, oldEntity);
        updateByIdSelective(oldEntity);
        return oldEntity;
    }

}

