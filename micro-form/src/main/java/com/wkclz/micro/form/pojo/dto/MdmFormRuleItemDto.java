package com.wkclz.micro.form.pojo.dto;

import com.wkclz.micro.form.pojo.entity.MdmFormRuleItem;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Description Create by shrimp-gen
 * @author wangkaicun
 * @table mdm_form_rule_item (表单校验规则-校验项) 数据库实例扩展，代码重新生成不覆盖
 */

@Data
@EqualsAndHashCode(callSuper = false)
public class MdmFormRuleItemDto extends MdmFormRuleItem {




    /**
     * entity 转 Dto
     * @param source
     * @return
     */
    public static MdmFormRuleItemDto copy(MdmFormRuleItem source) {
        MdmFormRuleItemDto target = new MdmFormRuleItemDto();
        MdmFormRuleItem.copy(source, target);
        return target;
    }
}

