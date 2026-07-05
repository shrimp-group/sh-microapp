package com.wkclz.micro.form.validator.impl;

import com.wkclz.micro.form.bean.dto.MdmFormRuleFieldValidatorDto;
import com.wkclz.micro.form.validator.IValidator;
import org.apache.commons.lang3.StringUtils;

import java.util.regex.Pattern;

/**
 * 模板验证器（使用正则表达式）
 */
public class TemplateValidator implements IValidator {

    @Override
    public String validate(String value, MdmFormRuleFieldValidatorDto validator) {
        if (StringUtils.isBlank(value)) {
            return null;
        }
        String validatorPattern = validator.getValidatorPattern();
        if (StringUtils.isBlank(validatorPattern)) {
            return "验证规则配置错误：缺少正则表达式模板";
        }
        try {
            if (!Pattern.matches(validatorPattern, value)) {
                return "格式不符合要求";
            }
        } catch (Exception e) {
            return "验证规则配置错误：正则表达式无效";
        }
        return null;
    }

}
