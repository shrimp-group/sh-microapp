package com.wkclz.micro.wxapp.bean.req.validation;

import com.wkclz.micro.wxapp.bean.req.WxMaUserInfoUpdateReq;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.apache.commons.lang3.StringUtils;

/**
 * 微信小程序用户信息更新请求校验器
 * nickname 和 avatar 至少要有一个不为空
 */
public class WxMaUserInfoUpdateValidator implements ConstraintValidator<WxMaUserInfoUpdateValid, WxMaUserInfoUpdateReq> {

    @Override
    public boolean isValid(WxMaUserInfoUpdateReq req, ConstraintValidatorContext context) {
        if (req == null) {
            return true;
        }
        return StringUtils.isNotBlank(req.getNickname()) || StringUtils.isNotBlank(req.getAvatar());
    }
}
