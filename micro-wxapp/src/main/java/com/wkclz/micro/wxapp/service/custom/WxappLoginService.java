package com.wkclz.micro.wxapp.service.custom;

import com.wkclz.iam.contract.bean.req.SessionCreateReq;
import com.wkclz.iam.contract.bean.resp.LoginResp;
import com.wkclz.iam.contract.facade.SsoFacadeContract;
import com.wkclz.micro.wxapp.bean.entity.WxappUser;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@AllArgsConstructor
public class WxappLoginService {

    @Autowired
    private SsoFacadeContract ssoFacadeContract;
    public LoginResp login(WxappUser user) {

        // 7. 登录成功，通过 SsoFacade 创建会话
        SessionCreateReq sessionCreateReq = new SessionCreateReq();
        sessionCreateReq.setUserCode(user.getUserCode());
        sessionCreateReq.setUsername(user.getOpenId());
        sessionCreateReq.setAuthIdentifier(user.getOpenId());
        sessionCreateReq.setNickname(user.getNickname());
        sessionCreateReq.setAvatar(user.getAvatar());
        sessionCreateReq.setAuthType("WXAPP");
        log.info("用户 {} 认证成功，调用 SsoFacade 创建会话", user.getOpenId());

        return ssoFacadeContract.login(sessionCreateReq);
    }

}
