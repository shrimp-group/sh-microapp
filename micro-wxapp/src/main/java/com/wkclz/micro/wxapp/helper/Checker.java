package com.wkclz.micro.wxapp.helper;

import java.util.regex.Pattern;

public class Checker {

    /**
     * 微信小程序 appId 正则表达式
     * 以 wx 开头，后面跟着 16 位字母或数字
     */
    private static final Pattern WX_APP_ID_PATTERN = Pattern.compile("^wx[a-fA-F0-9]{16}$");

    /**
     * 判断微信小程序 appId 是否合法
     *
     * @param appId 微信小程序的 appId
     * @return 合法返回 true，不合法返回 false
     */
    public static boolean isValidWxAppId(String appId) {
        if (appId == null || appId.isEmpty()) {
            return false;
        }
        return WX_APP_ID_PATTERN.matcher(appId).matches();
    }

}
