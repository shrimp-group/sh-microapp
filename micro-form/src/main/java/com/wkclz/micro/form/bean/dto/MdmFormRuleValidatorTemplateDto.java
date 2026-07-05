package com.wkclz.micro.form.bean.dto;

import com.wkclz.micro.form.bean.entity.MdmFormRuleValidatorTemplate;
import com.wkclz.tool.utils.BeanUtil;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Description Create by sh-generator
 * @author shrimp
 * @table MdmFormRuleValidatorTemplate () 数据库实例扩展，代码重新生成不覆盖
 */

@Data
@EqualsAndHashCode(callSuper = false)
public class MdmFormRuleValidatorTemplateDto extends MdmFormRuleValidatorTemplate {

    /**
     * entity 转 Dto
     * @param source
     * @return
     */
    public static MdmFormRuleValidatorTemplateDto copy(MdmFormRuleValidatorTemplate source) {
        return BeanUtil.cp(source, MdmFormRuleValidatorTemplateDto.class);
    }
}
