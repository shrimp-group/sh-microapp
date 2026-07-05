package com.wkclz.micro.form.validator.impl;

import com.wkclz.micro.form.bean.dto.MdmFormRuleFieldValidatorDto;
import com.wkclz.micro.form.validator.IValidator;
import org.apache.commons.lang3.StringUtils;

import java.util.regex.Pattern;

/**
 * 身份证号验证器
 */
public class IdCardValidator implements IValidator {

    private static final Pattern IDCARD_PATTERN = Pattern.compile(
        "(^\\d{15}$)|(^\\d{18}$)|(^\\d{17}(\\d|X|x)$)"
    );

    @Override
    public String validate(String value, MdmFormRuleFieldValidatorDto validator) {
        if (StringUtils.isBlank(value)) {
            return null;
        }
        if (!IDCARD_PATTERN.matcher(value).matches()) {
            return "身份证号格式不正确";
        }
        return null;
    }

}
