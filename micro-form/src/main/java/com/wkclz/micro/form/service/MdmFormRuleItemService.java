package com.wkclz.micro.form.service;

import com.wkclz.core.enums.ResultCode;
import com.wkclz.core.exception.ValidationException;
import com.wkclz.micro.form.dao.MdmFormRuleItemMapper;
import com.wkclz.micro.form.pojo.entity.MdmFormRuleItem;
import com.wkclz.mybatis.service.BaseService;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

/**
 * Description Create by shrimp-gen
 * @author wangkaicun
 * @table mdm_form_rule_item (表单校验规则-校验项) 单表服务类，代码重新生成不覆盖. 只建议完成单表的逻辑，或主表为 mdm_form_rule_item 的逻辑. 其他逻辑放 custom 中
 */
 
@Service
public class MdmFormRuleItemService extends BaseService<MdmFormRuleItem, MdmFormRuleItemMapper> {


    public MdmFormRuleItem create(MdmFormRuleItem entity) {
        mapper.insert(entity);
        return entity;
    }

    public MdmFormRuleItem update(MdmFormRuleItem entity) {
        Assert.notNull(entity.getId(), ResultCode.PARAM_NO_ID.getMessage());
        Assert.notNull(entity.getId(), ResultCode.UPDATE_NO_VERSION.getMessage());
        MdmFormRuleItem oldEntity = selectById(entity.getId());
        if (oldEntity == null) {
            throw ValidationException.of(ResultCode.RECORD_NOT_EXIST);
        }
        MdmFormRuleItem.copyIfNotNull(entity, oldEntity);
        updateByIdSelective(oldEntity);
        return oldEntity;
    }


}

