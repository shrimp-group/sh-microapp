package com.wkclz.micro.form.bean.dto;

import com.wkclz.micro.form.bean.entity.MdmFormRuleField;
import com.wkclz.tool.utils.BeanUtil;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * Description Create by sh-generator
 * @author shrimp
 * @table MdmFormRuleField () 数据库实例扩展，代码重新生成不覆盖
 */

@Data
@EqualsAndHashCode(callSuper = false)
public class MdmFormRuleFieldDto extends MdmFormRuleField {

    private List<MdmFormRuleFieldValidatorDto> validators;

    /**
     * entity 转 Dto
     * @param source
     * @return
     */
    public static MdmFormRuleFieldDto copy(MdmFormRuleField source) {
        return BeanUtil.cp(source, MdmFormRuleFieldDto.class);
    }
}
