package com.wkclz.micro.form.validator.impl;

import com.wkclz.micro.form.bean.dto.MdmFormRuleFieldValidatorDto;
import com.wkclz.micro.form.validator.IValidator;
import com.wkclz.tool.tools.RegularTool;
import org.apache.commons.lang3.StringUtils;

/**
 * 域名验证器
 */
public class DomainValidator implements IValidator {

    @Override
    public String validate(String value, MdmFormRuleFieldValidatorDto validator) {
        if (StringUtils.isBlank(value)) {
            return null;
        }
        if (!RegularTool.isDomain(value)) {
            return validator.getMsgTemplate();
        }
        return null;
    }

}
