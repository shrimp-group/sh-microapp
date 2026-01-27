package com.wkclz.micro.mask.service;

import com.wkclz.core.base.PageData;
import com.wkclz.core.enums.ResultCode;
import com.wkclz.core.exception.ValidationException;
import com.wkclz.micro.mask.dao.MdmMaskRuleMapper;
import com.wkclz.micro.mask.pojo.entity.MdmMaskRule;
import com.wkclz.mybatis.helper.PageQuery;
import com.wkclz.mybatis.service.BaseService;
import com.wkclz.redis.helper.RedisIdGenerator;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

/**
 * Description Create by shrimp-gen
 * @author wangkaicun
 * @table mdm_mask_rule (脱敏规则) 单表服务类，代码重新生成不覆盖. 只建议完成单表的逻辑，或主表为 mdm_mask_rule 的逻辑. 其他逻辑放 custom 中
 */
 
@Service
public class MdmMaskRuleService extends BaseService<MdmMaskRule, MdmMaskRuleMapper> {

    @Autowired
    private RedisIdGenerator redisIdGenerator;

    public PageData<MdmMaskRule> getMaskRulePage(MdmMaskRule entity) {
        return PageQuery.page(entity, mapper::getMaskRuleList);
    }

    public MdmMaskRule create(MdmMaskRule entity) {
        duplicateCheck(entity);
        if (StringUtils.isBlank(entity.getMaskRuleCode())) {
            entity.setMaskRuleCode(redisIdGenerator.generateIdWithPrefix("mask_rule_"));
        }
        mapper.insert(entity);
        return entity;
    }

    public MdmMaskRule update(MdmMaskRule entity) {
        duplicateCheck(entity);
        Assert.notNull(entity.getId(), ResultCode.PARAM_NO_ID.getMessage());
        Assert.notNull(entity.getId(), ResultCode.UPDATE_NO_VERSION.getMessage());
        MdmMaskRule oldEntity = selectById(entity.getId());
        if (oldEntity == null) {
            throw ValidationException.of(ResultCode.RECORD_NOT_EXIST);
        }
        MdmMaskRule.copyIfNotNull(entity, oldEntity);
        updateByIdSelective(oldEntity);
        return oldEntity;
    }

    private void duplicateCheck(MdmMaskRule entity) {
        // 唯一条件为空，直接通过
        if (StringUtils.isBlank(entity.getMaskRuleCode())) {
            return;
        }
        
        // 唯一条件不为空，请设置唯一条件
        MdmMaskRule param = new MdmMaskRule();
        // 唯一条件
        param.setMaskRuleCode(entity.getMaskRuleCode());
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

