package com.wkclz.micro.form.validator.impl;

import com.wkclz.micro.form.bean.dto.MdmFormRuleFieldValidatorDto;
import com.wkclz.micro.form.validator.IValidator;
import org.apache.commons.lang3.StringUtils;

import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * 时间验证器 (HH:mm:ss)
 */
public class TimeValidator implements IValidator {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss");

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
