package com.wkclz.micro.form.validator.impl;

import com.wkclz.micro.form.bean.dto.MdmFormRuleFieldValidatorDto;
import com.wkclz.micro.form.validator.IValidator;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;

/**
 * 浮点数大于验证器
 */
public class FloatGtValidator implements IValidator {

    @Override
    public String validate(String value, MdmFormRuleFieldValidatorDto validator) {
        if (StringUtils.isBlank(value)) {
            return null;
        }
        if (!NumberUtils.isCreatable(value)) {
            return "请输入有效的数字";
        }
        Double doubleValue = NumberUtils.createDouble(value);
        String pattern = validator.getValidatorPattern();
        if (StringUtils.isBlank(pattern)) {
            return "验证规则配置错误：缺少比较值";
        }
        Double compareValue = NumberUtils.createDouble(pattern);
        if (doubleValue <= compareValue) {
            return "必须大于 " + compareValue;
        }
        return null;
    }

}
