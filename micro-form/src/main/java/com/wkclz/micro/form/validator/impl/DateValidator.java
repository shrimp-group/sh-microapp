package com.wkclz.micro.form.validator.impl;

import com.wkclz.micro.form.bean.dto.MdmFormRuleFieldValidatorDto;
import com.wkclz.micro.form.validator.IValidator;
import org.apache.commons.lang3.StringUtils;

import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * 日期验证器 (yyyy-MM-dd)
 */
public class DateValidator implements IValidator {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @Override
    public String validate(String value, MdmFormRuleFieldValidatorDto validator) {
        if (StringUtils.isBlank(value)) {
            return null;
        }
        try {
            FORMATTER.parse(value);
        } catch (DateTimeParseException e) {
            return validator.getMsgTemplate();
        }
        return null;
    }

}
