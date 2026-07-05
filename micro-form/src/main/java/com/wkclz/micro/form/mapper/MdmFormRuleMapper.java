package com.wkclz.micro.form.mapper;

import com.wkclz.micro.form.bean.dto.MdmFormRuleDto;
import com.wkclz.mybatis.mapper.BaseMapper;
import com.wkclz.micro.form.bean.entity.MdmFormRule;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * Description Create by sh-generator
 * @author shrimp
 * @table mdm_form_rule (表单校验规则) Mapper 接口，代码重新生成不覆盖
 */

@Mapper
public interface MdmFormRuleMapper extends BaseMapper<MdmFormRule> {

    List<MdmFormRuleDto> getFormRuleList(MdmFormRuleDto dto);

    List<MdmFormRuleDto> get4Cache();

}

