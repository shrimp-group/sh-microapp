package com.wkclz.micro.pay.service;

import com.wkclz.core.enums.ResultCode;
import com.wkclz.core.exception.ValidationException;
import com.wkclz.iam.sdk.helper.SessionHelper;
import com.wkclz.micro.pay.bean.entity.PayOrder;
import com.wkclz.micro.pay.mapper.PayOrderMapper;
import com.wkclz.mybatis.service.BaseService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Description Create by shrimp-gen
 * @author wangkaicun
 * @table pay_order (支付-订单) 单表服务类，代码重新生成不覆盖. 只建议完成单表的逻辑，或主表为 pay_order 的逻辑. 其他逻辑放 custom 中
 */
 
@Service
public class PayOrderService extends BaseService<PayOrder, PayOrderMapper> {


    public List<PayOrder> getActivePayOrder(PayOrder entity) {
        return mapper.getActivePayOrder(entity);
    }

    public PayOrder getPayOrderStatus2Custom(PayOrder entity) {
        entity.setUserCode(SessionHelper.getUserCode());

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
        duplicateCheck(entity);
        mapper.insert(entity);
        return entity;
    }

    public PayOrder update(PayOrder entity) {
        duplicateCheck(entity);
        PayOrder oldEntity = selectById(entity.getId());
        if (oldEntity == null) {
            throw ValidationException.of(ResultCode.RECORD_NOT_EXIST);
        }
        PayOrder.copyIfNotNull(entity, oldEntity);
        updateByIdSelective(oldEntity);
        return oldEntity;
    }

    private void duplicateCheck(PayOrder entity) {
        // 唯一条件为空，直接通过
        if (true) {
            return;
        }
        
        // 唯一条件不为空，请设置唯一条件
        PayOrder param = new PayOrder();
        // 唯一条件
        param = selectOneByEntity(param);
        if (param == null) {
            return;
        }
        if (param.getId().equals(entity.getId())) {
            return;
        }
        // 查到有值，为新增或 id 不一样场景，为数据重复
        throw ValidationException.of(ResultCode.RECORD_DUPLICATE);
    }

}

