package com.wkclz.micro.form.validator.impl;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONException;
import com.wkclz.micro.form.bean.dto.MdmFormRuleFieldValidatorDto;
import com.wkclz.micro.form.validator.IValidator;
import org.apache.commons.lang3.StringUtils;

/**
 * JSON 验证器
 */
public class JsonValidator implements IValidator {

    @Override
    public String validate(String value, MdmFormRuleFieldValidatorDto validator) {
        if (StringUtils.isBlank(value)) {
            return null;
        }
        try {
            JSON.parse(value);
        } catch (JSONException e) {
            return "JSON 格式不正确";
        }
        return null;
    }

}
