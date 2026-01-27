package com.wkclz.micro.pay.service;

import com.wkclz.core.base.PageData;
import com.wkclz.core.enums.ResultCode;
import com.wkclz.core.exception.ValidationException;
import com.wkclz.micro.pay.dao.PayWxpayConfigMapper;
import com.wkclz.micro.pay.pojo.dto.PayWxpayConfigDto;
import com.wkclz.micro.pay.pojo.entity.PayWxpayConfig;
import com.wkclz.mybatis.helper.PageQuery;
import com.wkclz.mybatis.service.BaseService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

/**
 * Description Create by shrimp-gen
 * @author wangkaicun
 * @table pay_wxpay_config (支付-微信支付配置) 单表服务类，代码重新生成不覆盖. 只建议完成单表的逻辑，或主表为 pay_wxpay_config 的逻辑. 其他逻辑放 custom 中
 */
 
@Service
public class PayWxpayConfigService extends BaseService<PayWxpayConfig, PayWxpayConfigMapper> {

    public PageData<PayWxpayConfigDto> getWxpayConfigPage(PayWxpayConfigDto dto) {
        return PageQuery.page(dto, mapper::getWxpayConfigList);
    }

    public PayWxpayConfig getDetail(PayWxpayConfig entity) {
        entity = mapper.selectOneByEntity(entity);
        if (entity == null) {
            throw ValidationException.of(ResultCode.RECORD_NOT_EXIST);
        }
        if (StringUtils.isNotBlank(entity.getApiclientKey())) {
            entity.setApiclientKey("******");
        }
        if (StringUtils.isNotBlank(entity.getApiclientCert())) {
            entity.setApiclientCert("******");
        }
        if (StringUtils.isNotBlank(entity.getMchV3Key())) {
            entity.setMchV3Key("******");
        }
        return entity;
    }

    public PayWxpayConfig create(PayWxpayConfig entity) {
        duplicateCheck(entity);
        mapper.insert(entity);
        return entity;
    }

    public PayWxpayConfig update(PayWxpayConfig entity) {
        duplicateCheck(entity);
        Assert.notNull(entity.getId(), ResultCode.PARAM_NO_ID.getMessage());
        Assert.notNull(entity.getId(), ResultCode.UPDATE_NO_VERSION.getMessage());
        PayWxpayConfig oldEntity = selectById(entity.getId());
        if (oldEntity == null) {
            throw ValidationException.of(ResultCode.RECORD_NOT_EXIST);
        }
        if ("******".equals(entity.getApiclientKey())) {
            entity.setApiclientKey(null);
        }
        if ("******".equals(entity.getApiclientCert())) {
            entity.setApiclientCert(null);
        }
        if ("******".equals(entity.getMchV3Key())) {
            entity.setMchV3Key(null);
        }
        PayWxpayConfig.copyIfNotNull(entity, oldEntity);
        updateByIdSelective(oldEntity);
        return oldEntity;
    }

    private void duplicateCheck(PayWxpayConfig entity) {
        // 唯一条件不为空，请设置唯一条件
        PayWxpayConfig param = new PayWxpayConfig();
        param.setAppId(entity.getAppId());
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

