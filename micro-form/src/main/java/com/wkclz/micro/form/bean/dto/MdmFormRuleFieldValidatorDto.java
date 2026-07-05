package com.wkclz.micro.form.bean.dto;

import com.wkclz.micro.form.bean.entity.MdmFormRuleFieldValidator;
import com.wkclz.tool.utils.BeanUtil;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Description Create by sh-generator
 * @author shrimp
 * @table MdmFormRuleFieldValidator () 数据库实例扩展，代码重新生成不覆盖
 */

@Data
@EqualsAndHashCode(callSuper = false)
public class MdmFormRuleFieldValidatorDto extends MdmFormRuleFieldValidator {

    private String fieldName;

    /**
     * entity 转 Dto
     * @param source
     * @return
     */
    public static MdmFormRuleFieldValidatorDto copy(MdmFormRuleFieldValidator source) {
        return BeanUtil.cp(source, MdmFormRuleFieldValidatorDto.class);
    }
}
