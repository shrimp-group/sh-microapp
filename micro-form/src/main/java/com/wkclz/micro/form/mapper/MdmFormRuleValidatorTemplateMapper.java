package com.wkclz.micro.form.mapper;

import com.wkclz.micro.form.bean.entity.MdmFormRuleValidatorTemplate;
import com.wkclz.mybatis.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * Description Create by sh-generator
 * @author shrimp
 * @table mdm_form_rule_validator_template (表单校验规则-模板) Mapper 接口，代码重新生成不覆盖
 */

@Mapper
public interface MdmFormRuleValidatorTemplateMapper extends BaseMapper<MdmFormRuleValidatorTemplate> {

    List<MdmFormRuleValidatorTemplate> getValidatorTemplateList(MdmFormRuleValidatorTemplate entity);


}

