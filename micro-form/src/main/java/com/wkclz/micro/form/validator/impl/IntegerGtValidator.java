package com.wkclz.micro.form.validator.impl;

import com.wkclz.micro.form.bean.dto.MdmFormRuleFieldValidatorDto;
import com.wkclz.micro.form.validator.IValidator;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;

/**
 * 整数大于验证器
 */
public class IntegerGtValidator implements IValidator {

    @Override
    public String validate(String value, MdmFormRuleFieldValidatorDto validator) {
        if (StringUtils.isBlank(value)) {
            return null;
        }
        if (!NumberUtils.isCreatable(value)) {
            return "请输入有效的整数";
        }
        Integer intValue = NumberUtils.createInteger(value);
        String pattern = validator.getValidatorPattern();
        if (StringUtils.isBlank(pattern)) {
            return "验证规则配置错误：缺少比较值";
        }
        Integer compareValue = NumberUtils.createInteger(pattern);
        if (intValue <= compareValue) {
            return "必须大于 " + compareValue;
        }
        return null;
    }

}
