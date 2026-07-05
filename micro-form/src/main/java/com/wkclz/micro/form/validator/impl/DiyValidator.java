package com.wkclz.micro.form.validator.impl;

import com.alibaba.fastjson2.JSONObject;
import com.wkclz.micro.form.bean.dto.MdmFormRuleFieldValidatorDto;
import com.wkclz.micro.form.validator.IValidator;
import com.wkclz.tool.utils.JsUtil;
import org.apache.commons.lang3.StringUtils;

/**
 * 自定义验证器（使用 JavaScript 函数）
 */
public class DiyValidator implements IValidator {

    @Override
    public String validate(String value, MdmFormRuleFieldValidatorDto validator) {
        if (StringUtils.isBlank(value)) {
            return null;
        }

        String validatorPattern = validator.getValidatorPattern();

        if (StringUtils.isNotBlank(validatorPattern)) {
            if ( !value.matches(validatorPattern)) {
                return validator.getMsgTemplate();
            }
        }

        String validatorFunction = validator.getValidatorFunction();
        if (StringUtils.isNotBlank(validatorFunction)) {
            JSONObject jsonObject = JSONObject.parseObject(value);
            String exec = JsUtil.exec(validatorFunction, jsonObject);

            if ("true".equals(exec)) {
                return null;
            }

            if (StringUtils.isBlank(exec)) {
                exec = validator.getMsgTemplate();
            }
            if (StringUtils.isBlank(exec)) {
                exec = validator.getFieldName() + " 验证失败";
            }
            return exec;
        }
        return null;
    }


}
