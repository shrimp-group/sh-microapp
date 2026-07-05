package com.wkclz.micro.form.validator.impl;

import com.wkclz.micro.form.bean.dto.MdmFormRuleFieldValidatorDto;
import com.wkclz.micro.form.validator.IValidator;
import org.apache.commons.lang3.StringUtils;

import java.util.regex.Pattern;

/**
 * URL 验证器
 */
public class UrlValidator implements IValidator {

    private static final Pattern URL_PATTERN = Pattern.compile(
        "^(https?|ftp)://[^\\s/$.?#].[^\\s]*$", Pattern.CASE_INSENSITIVE
    );

    @Override
    public String validate(String value, MdmFormRuleFieldValidatorDto validator) {
        if (StringUtils.isBlank(value)) {
            return null;
        }
        if (!URL_PATTERN.matcher(value).matches()) {
            return "URL 格式不正确";
        }
        return null;
    }

}
