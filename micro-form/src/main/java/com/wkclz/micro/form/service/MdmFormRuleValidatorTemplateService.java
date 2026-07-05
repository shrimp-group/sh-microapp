package com.wkclz.micro.form.service;

import com.wkclz.core.base.PageData;
import com.wkclz.core.enums.ResultCode;
import com.wkclz.core.exception.UserException;
import com.wkclz.core.exception.ValidationException;
import com.wkclz.micro.form.cache.FormRuleCache;
import com.wkclz.micro.form.mapper.MdmFormRuleValidatorTemplateMapper;
import com.wkclz.micro.form.bean.entity.MdmFormRuleValidatorTemplate;
import com.wkclz.mybatis.helper.PageQuery;
import com.wkclz.mybatis.service.BaseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Description Create by sh-generator
 * @author shrimp
 * @table mdm_form_rule_validator_template (表单校验规则-模板) 单表服务类，代码重新生成不覆盖. 只建议完成单表的逻辑，或主表为 mdm_form_rule_validator_template 的逻辑. 其他逻辑放 custom 中
 */
 
@Service
public class MdmFormRuleValidatorTemplateService extends BaseService<MdmFormRuleValidatorTemplate, MdmFormRuleValidatorTemplateMapper> {

    @Autowired
    private FormRuleCache formRuleCache;

    public PageData<MdmFormRuleValidatorTemplate> getValidatorTemplatePage(MdmFormRuleValidatorTemplate entity) {
        return PageQuery.page(entity, mapper::getValidatorTemplateList);
    }


    public MdmFormRuleValidatorTemplate create(MdmFormRuleValidatorTemplate entity) {
        duplicateCheck(entity);
        mapper.insert(entity);
        formRuleCache.clearCache();
        return entity;
    }

    public MdmFormRuleValidatorTemplate update(MdmFormRuleValidatorTemplate entity) {
        duplicateCheck(entity);
        MdmFormRuleValidatorTemplate oldEntity = selectById(entity.getId());
        if (oldEntity == null) {
            throw ValidationException.of(ResultCode.RECORD_NOT_EXIST);
        }
        MdmFormRuleValidatorTemplate.copyIfNotNull(entity, oldEntity);
        updateByIdSelective(oldEntity);
        formRuleCache.clearCache();
        return oldEntity;
    }


    public MdmFormRuleValidatorTemplate remove(MdmFormRuleValidatorTemplate entity) {
        MdmFormRuleValidatorTemplate oldEntity = selectById(entity.getId());
        if (oldEntity == null) {
            throw ValidationException.of(ResultCode.RECORD_NOT_EXIST);
        }
        deleteById(oldEntity);
        formRuleCache.clearCache();
        return oldEntity;
    }

    private void duplicateCheck(MdmFormRuleValidatorTemplate entity) {
        // 唯一条件为空，直接通过
        if (true) {
            return;
        }
        
        // 唯一条件不为空，请设置唯一条件
        MdmFormRuleValidatorTemplate param = new MdmFormRuleValidatorTemplate();
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

