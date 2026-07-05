package com.wkclz.micro.form.validator;

import com.wkclz.micro.form.bean.enums.ValidatorTypeEnum;
import com.wkclz.micro.form.validator.impl.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 验证器工厂类
 */
public class ValidatorFactory {

    private static final Map<ValidatorTypeEnum, IValidator> VALIDATOR_MAP = new HashMap<>();

    static {
        VALIDATOR_MAP.put(ValidatorTypeEnum.REQUIRED, new RequiredValidator());

        VALIDATOR_MAP.put(ValidatorTypeEnum.INTEGER_GT, new IntegerGtValidator());
        VALIDATOR_MAP.put(ValidatorTypeEnum.INTEGER_GE, new IntegerGeValidator());
        VALIDATOR_MAP.put(ValidatorTypeEnum.INTEGER_LT, new IntegerLtValidator());
        VALIDATOR_MAP.put(ValidatorTypeEnum.INTEGER_LE, new IntegerLeValidator());

        VALIDATOR_MAP.put(ValidatorTypeEnum.FLOAT_GT, new FloatGtValidator());
        VALIDATOR_MAP.put(ValidatorTypeEnum.FLOAT_GE, new FloatGeValidator());
        VALIDATOR_MAP.put(ValidatorTypeEnum.FLOAT_LT, new FloatLtValidator());
        VALIDATOR_MAP.put(ValidatorTypeEnum.FLOAT_LE, new FloatLeValidator());

        VALIDATOR_MAP.put(ValidatorTypeEnum.STRING_GT, new StringGtValidator());
        VALIDATOR_MAP.put(ValidatorTypeEnum.STRING_GE, new StringGeValidator());
        VALIDATOR_MAP.put(ValidatorTypeEnum.STRING_LT, new StringLtValidator());
        VALIDATOR_MAP.put(ValidatorTypeEnum.STRING_LE, new StringLeValidator());

        VALIDATOR_MAP.put(ValidatorTypeEnum.DATE, new DateValidator());
        VALIDATOR_MAP.put(ValidatorTypeEnum.DATETIME, new DatetimeValidator());
        VALIDATOR_MAP.put(ValidatorTypeEnum.TIME, new TimeValidator());

        VALIDATOR_MAP.put(ValidatorTypeEnum.EMAIL, new EmailValidator());
        VALIDATOR_MAP.put(ValidatorTypeEnum.MOBILE, new MobileValidator());
        VALIDATOR_MAP.put(ValidatorTypeEnum.URL, new UrlValidator());
        VALIDATOR_MAP.put(ValidatorTypeEnum.DOMAIN, new DomainValidator());
        VALIDATOR_MAP.put(ValidatorTypeEnum.IP, new IpValidator());
        VALIDATOR_MAP.put(ValidatorTypeEnum.ID_CARD, new IdCardValidator());

        VALIDATOR_MAP.put(ValidatorTypeEnum.JSON, new JsonValidator());

        VALIDATOR_MAP.put(ValidatorTypeEnum.DIY, new DiyValidator());
        VALIDATOR_MAP.put(ValidatorTypeEnum.TEMPLATE, new TemplateValidator());
    }

    /**
     * 获取验证器
     *
     * @param validatorType 验证器类型
     * @return 验证器实例，未找到返回 null
     */
    public static IValidator getValidator(ValidatorTypeEnum validatorType) {
        return VALIDATOR_MAP.get(validatorType);
    }

    /**
     * 注册自定义验证器
     *
     * @param validatorType 验证器类型
     * @param validator     验证器实例
     */
    public static void registerValidator(ValidatorTypeEnum validatorType, IValidator validator) {
        VALIDATOR_MAP.put(validatorType, validator);
    }

}
