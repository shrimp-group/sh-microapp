package com.wkclz.micro.wxapp.service.custom;

import com.wkclz.auth.sdk.config.CasSdkConfig;
import com.wkclz.auth.sdk.helper.JwtHelper;
import com.wkclz.auth.sdk.pojo.LoginResponse;
import com.wkclz.auth.sdk.pojo.UserInfo;
import com.wkclz.micro.wxapp.bean.entity.WxappUser;
import lombok.AllArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
@AllArgsConstructor
public class WxappLoginService {

    private final CasSdkConfig casSdkConfig;
    private final StringRedisTemplate stringRedisTemplate;

    public LoginResponse login(WxappUser user) {
        String sessionId = UUID.randomUUID().toString().replace("-", "");
        UserInfo userInfo = new UserInfo();
        userInfo.setUserCode(user.getUserCode());
        userInfo.setUsername(user.getUserCode());
        userInfo.setNickname(user.getNickname());
        userInfo.setMobile(user.getMobile());
        userInfo.setEmail(user.getEmail());
        userInfo.setAvatar(user.getAvatar());
        userInfo.setSessionId(sessionId);
        userInfo.setAppCode("miniapp");

        LoginResponse response = new LoginResponse();
        response.setStatus(0);
        String token = JwtHelper.createToken(casSdkConfig.getTokenSecret(), userInfo);
        response.setToken(token);

        String value = System.currentTimeMillis() + "";
        stringRedisTemplate.boundValueOps(sessionId).set(value, 8L,  TimeUnit.HOURS);
        return response;
    }


}
