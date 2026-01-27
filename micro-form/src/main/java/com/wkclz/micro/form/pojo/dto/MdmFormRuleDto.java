package com.wkclz.micro.form.pojo.dto;

import com.wkclz.micro.form.pojo.entity.MdmFormRule;
import com.wkclz.micro.form.pojo.entity.MdmFormRuleItem;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * Description Create by shrimp-gen
 * @author wangkaicun
 * @table mdm_form_rule (表单校验规则) 数据库实例扩展，代码重新生成不覆盖
 */

@Data
@EqualsAndHashCode(callSuper = false)
public class MdmFormRuleDto extends MdmFormRule {

    // 规则数量
    private Integer itemCount;

    private List<MdmFormRuleItem> items;

    /**
     * entity 转 Dto
     * @param source
     * @return
     */
    public static MdmFormRuleDto copy(MdmFormRule source) {
        MdmFormRuleDto target = new MdmFormRuleDto();
        MdmFormRule.copy(source, target);
        return target;
    }
}

