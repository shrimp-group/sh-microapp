package com.wkclz.micro.form.mapper;

import com.wkclz.micro.form.bean.dto.MdmFormRuleFieldValidatorDto;
import com.wkclz.micro.form.bean.entity.MdmFormRule;
import com.wkclz.micro.form.bean.entity.MdmFormRuleFieldValidator;
import com.wkclz.mybatis.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * Description Create by sh-generator
 * @author shrimp
 * @table mdm_form_rule_field_validator (表单校验规则-验证器) Mapper 接口，代码重新生成不覆盖
 */

@Mapper
public interface MdmFormRuleFieldValidatorMapper extends BaseMapper<MdmFormRuleFieldValidator> {

    List<MdmFormRuleFieldValidatorDto> getFormRuleFieldValidatorList(@Param("formRuleCode") String formRuleCode);


    List<MdmFormRuleFieldValidatorDto> getFormRuleFieldValidatorList4Check(MdmFormRule entity);


    List<MdmFormRuleFieldValidatorDto> get4Cache();


}

