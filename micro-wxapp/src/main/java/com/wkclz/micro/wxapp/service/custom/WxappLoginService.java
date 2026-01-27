package com.wkclz.micro.wxapp.service.custom;

import com.alibaba.fastjson2.JSON;
import com.wkclz.iam.sdk.config.IamSdkConfig;
import com.wkclz.iam.sdk.enums.LoginStatus;
import com.wkclz.iam.sdk.model.LoginResponse;
import com.wkclz.iam.sdk.model.UserJwt;
import com.wkclz.iam.sdk.model.UserSession;
import com.wkclz.iam.sdk.util.JwtUtil;
import com.wkclz.micro.wxapp.bean.entity.WxappUser;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class WxappLoginService {

    @Autowired
    private IamSdkConfig iamSdkConfig;
    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    public LoginResponse login(WxappUser user) {
        LoginResponse response = new LoginResponse();

        // JWT, 生成 token 返回给前端
        UserJwt jwt = new UserJwt();
        jwt.setUserCode(user.getUserCode());
        jwt.setUsername(user.getOpenId());
        jwt.setNickname(user.getNickname());
        jwt.setAvatar(user.getAvatar());
        String jwtToken = JwtUtil.generateToken(jwt, iamSdkConfig.getJwtSecretKey());

        // 用户信息，缓存到 Redis
        UserSession us = new UserSession();
        us.setUserCode(user.getUserCode());
        us.setUsername(user.getOpenId());
        us.setNickname(user.getNickname());
        us.setAuthType("WXAPP");

        String tokenRedisKey = JwtUtil.getTokenRedisKey(jwtToken, jwt.getUsername());
        redisTemplate.opsForValue().set(tokenRedisKey, JSON.toJSONString(us));

        response.setLoginStatus(LoginStatus.SUCCESS.getCode());
        response.setLoginMessage(LoginStatus.SUCCESS.getMessage());
        response.setToken(jwtToken);
        return response;
    }

}
