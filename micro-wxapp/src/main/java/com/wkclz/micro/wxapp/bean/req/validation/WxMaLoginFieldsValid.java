package com.wkclz.micro.wxapp.bean.req.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 微信小程序登录请求字段条件校验
 * encryptedData、iv、rawData、signature 四个字段要么全部提供，要么全部不提供
 */
@Documented
@Constraint(validatedBy = WxMaLoginFieldsValidator.class)
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface WxMaLoginFieldsValid {

    String message() default "用户信息字段(encryptedData、iv、rawData、signature)必须全部提供或全部不提供";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
