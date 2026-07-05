package com.wkclz.micro.wxapp.bean.req.validation;

import com.wkclz.micro.wxapp.bean.req.WxMaLoginReq;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.apache.commons.lang3.StringUtils;

/**
 * 微信小程序登录请求字段条件校验器
 * encryptedData、iv、rawData、signature 四个字段要么全部提供，要么全部不提供
 */
public class WxMaLoginFieldsValidator implements ConstraintValidator<WxMaLoginFieldsValid, WxMaLoginReq> {

    @Override
    public boolean isValid(WxMaLoginReq req, ConstraintValidatorContext context) {
        if (req == null) {
            return true;
        }

        boolean hasEncryptedData = StringUtils.isNotBlank(req.getEncryptedData());
        boolean hasIv = StringUtils.isNotBlank(req.getIv());
        boolean hasRawData = StringUtils.isNotBlank(req.getRawData());
        boolean hasSignature = StringUtils.isNotBlank(req.getSignature());

        int count = 0;
        if (hasEncryptedData) { count++; }
        if (hasIv) { count++; }
        if (hasRawData) { count++; }
        if (hasSignature) { count++; }

        // 全部为空或全部不为空时合法
        return count == 0 || count == 4;
    }
}
