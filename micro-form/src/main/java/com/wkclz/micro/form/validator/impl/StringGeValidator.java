package com.wkclz.micro.form.validator.impl;

import com.wkclz.micro.form.bean.dto.MdmFormRuleFieldValidatorDto;
import com.wkclz.micro.form.validator.IValidator;
import org.apache.commons.lang3.StringUtils;

/**
 * 字符串长度大于等于验证器
 */
public class StringGeValidator implements IValidator {

    @Override
    public String validate(String value, MdmFormRuleFieldValidatorDto validator) {
        if (StringUtils.isBlank(value)) {
            return null;
        }
        String pattern = validator.getValidatorPattern();
        if (StringUtils.isBlank(pattern)) {
            return "验证规则配置错误：缺少长度限制值";
        }
        try {
            int minLength = Integer.parseInt(pattern);
            if (value.length() < minLength) {
                return "长度必须大于或等于 " + minLength + " 个字符";
            }
        } catch (NumberFormatException e) {
            return "验证规则配置错误：长度限制值必须是整数";
        }
        return null;
    }

}
