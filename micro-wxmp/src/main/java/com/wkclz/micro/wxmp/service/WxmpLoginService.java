package com.wkclz.micro.wxmp.service;

import com.alibaba.fastjson2.JSON;
import com.wkclz.core.exception.ValidationException;
import com.wkclz.iam.sdk.bean.UserJwt;
import com.wkclz.iam.sdk.bean.UserSession;
import com.wkclz.iam.sdk.config.IamSdkConfig;
import com.wkclz.iam.sdk.util.JwtUtil;
import com.wkclz.micro.wxmp.config.WxMpConfiguration;
import com.wkclz.micro.wxmp.bean.entity.WxmpLoginLog;
import com.wkclz.micro.wxmp.bean.entity.WxmpUser;
import com.wkclz.micro.wxmp.bean.resp.WxmpLoginResp;
import com.wkclz.web.helper.IpHelper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import me.chanjar.weixin.common.bean.WxOAuth2UserInfo;
import me.chanjar.weixin.common.bean.oauth2.WxOAuth2AccessToken;
import me.chanjar.weixin.common.error.WxErrorException;
import me.chanjar.weixin.mp.api.WxMpService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class WxmpLoginService {

    @Autowired
    private IamSdkConfig iamSdkConfig;
    @Autowired
    private WxmpUserService wxmpUserService;
    @Autowired
    private WxMpConfiguration wxMpConfiguration;
    @Autowired
    private WxmpLoginLogService wxmpLoginLogService;
    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    /**
     * 微信公众号OAuth2登录
     */
    public WxmpLoginResp login(String appId, String code, HttpServletRequest req) {
        log.info("微信公众号登录, appId: {}", appId);

        WxMpService mpService = wxMpConfiguration.getMpService(appId);
        if (mpService == null) {
            throw ValidationException.of("未找到对应appid=[%s]的配置，请核实！", appId);
        }

        WxOAuth2UserInfo wxUser;
        try {
            WxOAuth2AccessToken accessToken = mpService.getOAuth2Service().getAccessToken(code);
            wxUser = mpService.getOAuth2Service().getUserInfo(accessToken, "zh_CN");
        } catch (WxErrorException e) {
            log.error("微信公众号登录失败, appId: {}, error: {}", appId, e.getError(), e);
            throw ValidationException.of("登录失败: %s", e.getError().getErrorMsg());
        }

        // 转换微信用户信息
        WxmpUser user = new WxmpUser();
        user.setOpenId(wxUser.getOpenid());
        user.setUnionId(wxUser.getUnionId());
        user.setNickname(wxUser.getNickname());
        user.setGender(wxUser.getSex());
        user.setAvatar(wxUser.getHeadImgUrl());
        user.setCountry(wxUser.getCountry());
        user.setProvince(wxUser.getProvince());
        user.setCity(wxUser.getCity());
        user.setAppId(appId);
        if (wxUser.getPrivileges() != null && wxUser.getPrivileges().length > 0) {
            user.setPrivilegeList(StringUtils.join(wxUser.getPrivileges(), ","));
        }

        // 初始化/更新用户
        user = wxmpUserService.initUser(user);
        log.info("微信用户登录, userCode: {}, openId: {}", user.getUserCode(), user.getOpenId());

        // 生成JWT令牌
        UserJwt jwt = new UserJwt();
        jwt.setUserCode(user.getUserCode());
        jwt.setUsername(user.getUserCode());
        jwt.setNickname(user.getNickname());
        jwt.setAvatar(user.getAvatar());
        String jwtToken = JwtUtil.generateToken(jwt, iamSdkConfig.getJwtSecretKey());

        // 缓存用户会话到Redis
        UserSession us = new UserSession();
        us.setUserCode(user.getUserCode());
        us.setUsername(user.getUserCode());
        us.setNickname(user.getNickname());
        us.setAuthType("WXMP");
        String tokenRedisKey = JwtUtil.getTokenRedisKey(jwtToken, jwt.getUsername());
        redisTemplate.opsForValue().set(tokenRedisKey, JSON.toJSONString(us));

        // 记录登录日志
        WxmpLoginLog loginLog = new WxmpLoginLog();
        loginLog.setUserCode(user.getUserCode());
        loginLog.setOpenId(user.getOpenId());
        loginLog.setLoginIp(IpHelper.getOriginIp(req));
        wxmpLoginLogService.insert(loginLog);

        // 构建响应
        WxmpLoginResp resp = new WxmpLoginResp();
        resp.setToken(jwtToken);
        resp.setUserCode(user.getUserCode());
        resp.setOpenId(user.getOpenId());
        resp.setNickname(user.getNickname());
        resp.setAvatar(user.getAvatar());

        log.info("微信公众号登录成功, userCode: {}", user.getUserCode());
        return resp;
    }
}
