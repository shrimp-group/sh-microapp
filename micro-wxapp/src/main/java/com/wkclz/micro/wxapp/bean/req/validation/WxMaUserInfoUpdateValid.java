package com.wkclz.micro.wxapp.bean.req.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 微信小程序用户信息更新请求校验
 * nickname 和 avatar 至少要有一个不为空
 */
@Documented
@Constraint(validatedBy = WxMaUserInfoUpdateValidator.class)
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface WxMaUserInfoUpdateValid {

    String message() default "没有头像也没有昵称！";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
