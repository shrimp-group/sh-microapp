package com.wkclz.micro.pay.service;

import com.wkclz.core.base.PageData;
import com.wkclz.core.enums.ResultCode;
import com.wkclz.core.exception.ValidationException;
import com.wkclz.micro.pay.dao.PayAlipayConfigMapper;
import com.wkclz.micro.pay.pojo.dto.PayAlipayConfigDto;
import com.wkclz.micro.pay.pojo.entity.PayAlipayConfig;
import com.wkclz.mybatis.helper.PageQuery;
import com.wkclz.mybatis.service.BaseService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

/**
 * Description Create by shrimp-gen
 * @author wangkaicun
 * @table pay_alipay_config (支付-支付宝配置) 单表服务类，代码重新生成不覆盖. 只建议完成单表的逻辑，或主表为 pay_alipay_config 的逻辑. 其他逻辑放 custom 中
 */
 
@Service
public class PayAlipayConfigService extends BaseService<PayAlipayConfig, PayAlipayConfigMapper> {

    public PageData<PayAlipayConfigDto> getAlipayConfigPage(PayAlipayConfigDto dto) {
        return PageQuery.page(dto, mapper::getAlipayConfigList);
    }

    public PayAlipayConfig getDetail(PayAlipayConfig entity) {
        entity = mapper.selectOneByEntity(entity);
        if (entity == null) {
            throw ValidationException.of(ResultCode.RECORD_NOT_EXIST);
        }
        if (StringUtils.isNotBlank(entity.getMerchantPrivateKey())) {
            entity.setMerchantPrivateKey("******");
        }
        return entity;
    }

    public PayAlipayConfig create(PayAlipayConfig entity) {
        duplicateCheck(entity);
        mapper.insert(entity);
        return entity;
    }

    public PayAlipayConfig update(PayAlipayConfig entity) {
        duplicateCheck(entity);
        Assert.notNull(entity.getId(), ResultCode.PARAM_NO_ID.getMessage());
        Assert.notNull(entity.getId(), ResultCode.UPDATE_NO_VERSION.getMessage());
        PayAlipayConfig oldEntity = selectById(entity.getId());
        if (oldEntity == null) {
            throw ValidationException.of(ResultCode.RECORD_NOT_EXIST);
        }
        if ("******".equals(entity.getMerchantPrivateKey())) {
            entity.setMerchantPrivateKey(null);
        }
        PayAlipayConfig.copyIfNotNull(entity, oldEntity);
        updateByIdSelective(oldEntity);
        return oldEntity;
    }

    private void duplicateCheck(PayAlipayConfig entity) {
        // 唯一条件不为空，请设置唯一条件
        PayAlipayConfig param = new PayAlipayConfig();
        // 唯一条件
        param.setAppId(entity.getAppId());
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

