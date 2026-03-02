package com.wkclz.micro.wxmp.rest.cust;

import com.alibaba.fastjson2.JSON;
import com.wkclz.core.base.R;
import com.wkclz.iam.sdk.config.IamSdkConfig;
import com.wkclz.iam.sdk.enums.LoginStatus;
import com.wkclz.iam.sdk.model.LoginResponse;
import com.wkclz.iam.sdk.model.UserJwt;
import com.wkclz.iam.sdk.model.UserSession;
import com.wkclz.iam.sdk.util.JwtUtil;
import com.wkclz.micro.wxmp.config.WxMpConfiguration;
import com.wkclz.micro.wxmp.dao.WxmpLoginLogMapper;
import com.wkclz.micro.wxmp.pojo.entity.WxmpLoginLog;
import com.wkclz.micro.wxmp.pojo.entity.WxmpUser;
import com.wkclz.micro.wxmp.rest.Route;
import com.wkclz.micro.wxmp.service.WxmpUserService;
import com.wkclz.web.helper.IpHelper;
import jakarta.servlet.http.HttpServletRequest;
import me.chanjar.weixin.common.bean.WxOAuth2UserInfo;
import me.chanjar.weixin.common.bean.oauth2.WxOAuth2AccessToken;
import me.chanjar.weixin.common.error.WxErrorException;
import me.chanjar.weixin.mp.api.WxMpService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;

/**
 * @author Edward
 */
@RestController
@RequestMapping(Route.PREFIX)
public class WxmpLoginRest {

    @Autowired
    private IamSdkConfig iamSdkConfig;
    @Autowired
    private WxmpUserService wxmpUserService;
    @Autowired
    private WxMpConfiguration wxMpConfiguration;
    @Autowired
    private WxmpLoginLogMapper wxmpLoginLogMapper;
    @Autowired
    private RedisTemplate<String, String> redisTemplate;



    /**
     * @api {get} /public/wxmp/login/{appid} 微信公众号授权登录
     * @apiGroup WXMP
     *
     * @apiVersion 0.0.1
     * @apiDescription 微信公众号授权登录
     *
     * @apiParam {String} code 微信授权时的code
     *
     * @apiParamExample {param} 请求样例:
     * ?code=xxxxxx
     *
     * @apiSuccess {Integer} status 登录状态(0成功)
     * @apiSuccess {String} token 登录成功后的Token,其他请求需要附加到header
     *
     * @apiSuccessExample {json} 返回样例:
     * {
     *     "code": 1,
     *     "data": {
     *         "status": 0
     *         "token": "xxxxxxxxxx"
     *     }
     * }
     *
     */
    @GetMapping(Route.PUBLIC_WXMP_LOGIN_APPID)
    public R publicWxmpLoginAppid(@PathVariable("appid") String appid, @RequestParam("code") String code, ModelMap map, HttpServletRequest req) {
        WxMpService mpService = wxMpConfiguration.getMpService(appid);
        if (mpService == null) {
            return R.warn("未找到对应appid=[%s]的配置，请核实！", appid);
        }
        WxOAuth2UserInfo user;
        try {
            WxOAuth2AccessToken accessToken = mpService.getOAuth2Service().getAccessToken(code);
            user = mpService.getOAuth2Service().getUserInfo(accessToken, "zh_CN");
        } catch (WxErrorException e) {
            return R.error("登录失败: {}", e.getError());
        }
        WxmpUser u = new WxmpUser();
        u.setOpenId(user.getOpenid());
        u.setUnionId(user.getUnionId());
        u.setNickname(user.getNickname());
        u.setGender(user.getSex());
        u.setAvatar(user.getHeadImgUrl());
        u.setCountry(user.getCountry());
        u.setProvince(user.getProvince());
        u.setCity(user.getCity());
        u.setAppId(appid);
        if (user.getPrivileges() != null && user.getPrivileges().length > 0) {
            u.setPrivilegeList(StringUtils.join(user.getPrivileges(), ","));
        }
        u = wxmpUserService.initUser(u);
        // 登录
        LoginResponse response = login(u, req);
        return R.ok(response);
    }


    private LoginResponse login(WxmpUser user, HttpServletRequest req) {
        LoginResponse response = new LoginResponse();


        // JWT, 生成 token 返回给前端
        UserJwt jwt = new UserJwt();
        jwt.setUserCode(user.getUserCode());
        jwt.setUsername(user.getUserCode());
        jwt.setNickname(user.getNickname());
        jwt.setAvatar(user.getAvatar());
        String jwtToken = JwtUtil.generateToken(jwt, iamSdkConfig.getJwtSecretKey());

        // 用户信息，缓存到 Redis
        UserSession us = new UserSession();
        us.setUserCode(user.getUserCode());
        us.setUsername(user.getUserCode());
        us.setNickname(user.getNickname());
        us.setAuthType("WXMP");

        String tokenRedisKey = JwtUtil.getTokenRedisKey(jwtToken, jwt.getUsername());
        redisTemplate.opsForValue().set(tokenRedisKey, JSON.toJSONString(us));

        response.setLoginStatus(LoginStatus.SUCCESS.getCode());
        response.setLoginMessage(LoginStatus.SUCCESS.getMessage());
        response.setToken(jwtToken);

        WxmpLoginLog log = new WxmpLoginLog();
        log.setUserCode(user.getUserCode());
        log.setOpenId(user.getOpenId());
        log.setLoginIp(IpHelper.getOriginIp(req));
        wxmpLoginLogMapper.insert(log);

        return response;
    }

}
