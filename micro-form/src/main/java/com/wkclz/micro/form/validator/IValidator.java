package com.wkclz.micro.form.validator;

import com.wkclz.micro.form.bean.dto.MdmFormRuleFieldValidatorDto;

/**
 * 验证器接口
 */
public interface IValidator {

    /**
     * 执行验证
     *
     * @param value     字段值
     * @param validator 验证器配置
     * @return 验证结果，null 表示验证通过，否则返回错误信息
     */
    String validate(String value, MdmFormRuleFieldValidatorDto validator);

}
