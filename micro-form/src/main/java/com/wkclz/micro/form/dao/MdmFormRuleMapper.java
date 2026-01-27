package com.wkclz.micro.form.dao;

import com.wkclz.micro.form.pojo.dto.MdmFormRuleDto;
import com.wkclz.micro.form.pojo.entity.MdmFormRule;
import com.wkclz.mybatis.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * Description Create by shrimp-gen
 * @author wangkaicun
 * @table mdm_form_rule (表单校验规则) DAO 接口，代码重新生成不覆盖
 */

@Mapper
public interface MdmFormRuleMapper extends BaseMapper<MdmFormRule> {

    List<MdmFormRuleDto> getFormRuleList(MdmFormRuleDto dto);

    List<MdmFormRule> get4Cache();

}

