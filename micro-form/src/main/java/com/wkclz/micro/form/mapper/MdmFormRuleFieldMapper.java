package com.wkclz.micro.form.mapper;

import com.wkclz.micro.form.bean.dto.MdmFormRuleFieldDto;
import com.wkclz.micro.form.bean.entity.MdmFormRuleField;
import com.wkclz.mybatis.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * Description Create by sh-generator
 * @author shrimp
 * @table mdm_form_rule_field (表单校验规则-校验项) Mapper 接口，代码重新生成不覆盖
 */

@Mapper
public interface MdmFormRuleFieldMapper extends BaseMapper<MdmFormRuleField> {

    List<MdmFormRuleFieldDto> get4Cache();

}

