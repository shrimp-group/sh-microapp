package com.wkclz.micro.wxapp.service.custom;

import com.wkclz.core.identity.UserIdentity;
import com.wkclz.iam.session.bean.SessionCreateResult;
import com.wkclz.iam.session.bean.resp.LoginResp;
import com.wkclz.iam.session.enums.AuthType;
import com.wkclz.iam.session.enums.LoginStatus;
import com.wkclz.iam.session.service.SessionManager;
import com.wkclz.micro.wxapp.bean.entity.WxappUser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class WxappLoginService {

    @Autowired
    private SessionManager sessionManager;

    public LoginResp login(WxappUser user) {

        UserIdentity identity = new UserIdentity();
        identity.setUserCode(user.getUserCode());
        identity.setUsername(user.getOpenId());
        identity.setNickname(user.getNickname());
        identity.setAvatar(user.getAvatar());
        identity.addAttribute("open-id", user.getOpenId());

        log.info("用户 {} 认证成功，通过 SessionManager 创建会话", user.getOpenId());

        SessionCreateResult result = sessionManager.createSession(identity, AuthType.WECHAT_MINI);

        LoginResp resp = new LoginResp();
        resp.setToken(result.getToken());
        resp.setUserCode(user.getUserCode());
        resp.setUsername(user.getOpenId());
        resp.setNickname(user.getNickname());
        resp.setAvatar(user.getAvatar());
        resp.setLoginStatus(LoginStatus.SUCCESS.getCode());
        resp.setLoginMessage(LoginStatus.SUCCESS.getMessage());
        return resp;
    }
}
