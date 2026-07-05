package com.wkclz.micro.form.service;

import com.wkclz.core.enums.ResultCode;
import com.wkclz.core.exception.UserException;
import com.wkclz.core.exception.ValidationException;
import com.wkclz.micro.form.mapper.MdmFormRuleFieldMapper;
import com.wkclz.micro.form.bean.entity.MdmFormRuleField;
import com.wkclz.mybatis.service.BaseService;
import org.springframework.stereotype.Service;

/**
 * Description Create by sh-generator
 * @author shrimp
 * @table mdm_form_rule_field (表单校验规则-校验项) 单表服务类，代码重新生成不覆盖. 只建议完成单表的逻辑，或主表为 mdm_form_rule_field 的逻辑. 其他逻辑放 custom 中
 */
 
@Service
public class MdmFormRuleFieldService extends BaseService<MdmFormRuleField, MdmFormRuleFieldMapper> {


    public MdmFormRuleField create(MdmFormRuleField entity) {
        duplicateCheck(entity);
        mapper.insert(entity);
        return entity;
    }

    public MdmFormRuleField update(MdmFormRuleField entity) {
        duplicateCheck(entity);
        MdmFormRuleField oldEntity = selectById(entity.getId());
        if (oldEntity == null) {
            throw ValidationException.of(ResultCode.RECORD_NOT_EXIST);
        }
        MdmFormRuleField.copyIfNotNull(entity, oldEntity);
        updateByIdSelective(oldEntity);
        return oldEntity;
    }

    public MdmFormRuleField save(MdmFormRuleField entity) {
        return entity.getId() == null ? create(entity) : update(entity);
    }

    public MdmFormRuleField remove(MdmFormRuleField entity) {
        MdmFormRuleField oldEntity = selectById(entity.getId());
        if (oldEntity == null) {
            throw ValidationException.of(ResultCode.RECORD_NOT_EXIST);
        }
        deleteById(oldEntity);
        return oldEntity;
    }

    private void duplicateCheck(MdmFormRuleField entity) {
        // 唯一条件为空，直接通过
        if (true) {
            return;
        }
        
        // 唯一条件不为空，请设置唯一条件
        MdmFormRuleField param = new MdmFormRuleField();
        // 唯一条件
        param = selectOneByEntity(param);
        if (param == null) {
            return;
        }
        if (param.getId().equals(entity.getId())) {
            return;
        }
        // 查到有值，为新增或 id 不一样场景，为数据重复
        throw UserException.of(ResultCode.RECORD_DUPLICATE);
    }

}

