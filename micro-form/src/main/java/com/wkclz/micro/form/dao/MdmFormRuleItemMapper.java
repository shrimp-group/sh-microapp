package com.wkclz.micro.form.dao;

import com.wkclz.micro.form.pojo.entity.MdmFormRule;
import com.wkclz.micro.form.pojo.entity.MdmFormRuleItem;
import com.wkclz.mybatis.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * Description Create by shrimp-gen
 * @author wangkaicun
 * @table mdm_form_rule_item (表单校验规则-校验项) DAO 接口，代码重新生成不覆盖
 */

@Mapper
public interface MdmFormRuleItemMapper extends BaseMapper<MdmFormRuleItem> {

    List<MdmFormRuleItem> get4Cache();

    List<MdmFormRuleItem> getFormRule4Check(MdmFormRule entity);


}

