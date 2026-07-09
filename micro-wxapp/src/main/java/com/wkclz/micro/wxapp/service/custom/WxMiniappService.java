package com.wkclz.micro.wxapp.service.custom;

import cn.binarywang.wx.miniapp.api.WxMaService;
import cn.binarywang.wx.miniapp.bean.WxMaJscode2SessionResult;
import cn.binarywang.wx.miniapp.bean.WxMaPhoneNumberInfo;
import cn.binarywang.wx.miniapp.bean.WxMaUserInfo;
import com.wkclz.core.exception.ValidationException;
import com.wkclz.iam.contract.bean.resp.LoginResp;
import com.wkclz.iam.contract.facade.SsoFacadeContract;
import com.wkclz.iam.contract.context.PrincipalContext;
import com.wkclz.micro.fileos.api.FileosSignApi;
import com.wkclz.micro.wxapp.bean.entity.WxappLoginLog;
import com.wkclz.micro.wxapp.bean.entity.WxappUser;
import com.wkclz.micro.wxapp.bean.req.WxMaLoginReq;
import com.wkclz.micro.wxapp.bean.req.WxMaMobileBindReq;
import com.wkclz.micro.wxapp.bean.req.WxMaPhoneReq;
import com.wkclz.micro.wxapp.bean.resp.WxMaUserInfoResp;
import com.wkclz.micro.wxapp.config.WxMaConfiguration;
import com.wkclz.micro.wxapp.mapper.WxappLoginLogMapper;
import com.wkclz.micro.wxapp.mapper.WxappUserMapper;
import com.wkclz.micro.wxapp.service.WxappUserService;
import com.wkclz.redis.helper.RedisIdGenerator;
import com.wkclz.tool.tools.RegularTool;
import com.wkclz.web.helper.IpHelper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.chanjar.weixin.common.error.WxErrorException;
import org.apache.commons.lang3.StringUtils;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@AllArgsConstructor
public class WxMiniappService {

    private final SsoFacadeContract ssoFacadeContract;
    private final FileosSignApi fileosSignApi;
    private final WxMaConfiguration configuration;
    private final WxappUserMapper wxappUserMapper;
    private final WxappUserService wxappUserService;
    private final WxappLoginLogMapper wxappLoginLogMapper;
    private final RedisIdGenerator redisIdGenerator;
    private final WxappLoginService wxappLoginService;

    @Transactional(rollbackFor = Exception.class)
    public LoginResp miniappLogin(WxMaLoginReq req, HttpServletRequest httpReq) {
        boolean withUserInfo = StringUtils.isNotBlank(req.getEncryptedData())
            && StringUtils.isNotBlank(req.getIv())
            && StringUtils.isNotBlank(req.getRawData())
            && StringUtils.isNotBlank(req.getSignature());

        WxMaService wxService = configuration.getMaService(req.getAppId());
        WxMaJscode2SessionResult session = null;
        try {
            session = wxService.getUserService().getSessionInfo(req.getCode());
        } catch (WxErrorException e) {
            throw new RuntimeException(e);
        }

        String openid = session.getOpenid();
        String unionid = session.getUnionid();
        String sessionKey = session.getSessionKey();
        log.info("openid:{}, unionid:{}, sessionKey:{}", openid, unionid, sessionKey);

        WxMaUserInfo wxMaUserInfo = null;
        if (withUserInfo) {
            // 用户信息校验
            if (!wxService.getUserService().checkUserInfo(sessionKey, req.getRawData(), req.getSignature())) {
                throw ValidationException.of("Wxapp check failed");
            }
            // 解密用户信息
            wxMaUserInfo = wxService.getUserService().getUserInfo(sessionKey, req.getEncryptedData(), req.getIv());
        }

        WxappUser user = wxappUserMapper.getWxappUserByOpenId(openid);
        // 第一次登录，什么信息都没有
        if (user == null) {
            String userCode = wxMaUserInfo == null ? redisIdGenerator.generateIdWithPrefix("wxapp_") : wxMaUserInfo.getNickName();

            user = new WxappUser();
            user.setAppId(req.getAppId());
            user.setOpenId(openid);
            user.setUnionId(unionid);
            user.setUserCode(userCode);
            user.setNickname(userCode);
            user.setLoginTimes(0);

            if (wxMaUserInfo != null) {
                user.setAvatar(wxMaUserInfo.getAvatarUrl());
                user.setGender(getUserGenderFromWechat(wxMaUserInfo));
                /*
                user.setCity(wxMaUserInfo.getCity());
                user.setProvince(wxMaUserInfo.getProvince());
                user.setCountry(wxMaUserInfo.getCountry());
                */
            }

            try {
                wxappUserMapper.insert(user);
                log.info("新用户注册成功，openId: {}, userCode: {}", openid, user.getUserCode());
            } catch (DuplicateKeyException e) {
                log.warn("并发登录导致重复插入，openId: {}, 查询已有记录", openid);
                user = wxappUserMapper.getWxappUserByOpenId(openid);
                if (user == null) {
                    throw ValidationException.of("用户注册异常，请重试");
                }
            }
        } else {
            // 只有在 wxMaUserInfo 不为空的时候才尝试修改信息
            if (wxMaUserInfo != null) {
                boolean userChangeFlag = false;
                if (StringUtils.isBlank(user.getNickname()) || !user.getNickname().equals(wxMaUserInfo.getNickName())) {
                    user.setNickname(wxMaUserInfo.getNickName());
                    userChangeFlag = true;
                }
                if (StringUtils.isBlank(user.getAvatar()) || !user.getAvatar().equals(wxMaUserInfo.getAvatarUrl())) {
                    user.setAvatar(wxMaUserInfo.getAvatarUrl());
                    userChangeFlag = true;
                }
                /*
                if (StringUtils.isBlank(user.getCity()) || !user.getCity().equals(wxMaUserInfo.getCity())) {
                    user.setCity(wxMaUserInfo.getCity());
                    userChangeFlag = true;
                }
                if (StringUtils.isBlank(user.getProvince()) || !user.getProvince().equals(wxMaUserInfo.getProvince())) {
                    user.setProvince(wxMaUserInfo.getProvince());
                    userChangeFlag = true;
                }
                if (StringUtils.isBlank(user.getCountry()) || !user.getCountry().equals(wxMaUserInfo.getCountry())) {
                    user.setCountry(wxMaUserInfo.getCountry());
                    userChangeFlag = true;
                }
                */
                Integer gender = getUserGenderFromWechat(wxMaUserInfo);
                if (user.getGender() == null || !user.getGender().equals(gender)){
                    user.setGender(gender);
                    userChangeFlag = true;
                }
                if (userChangeFlag){
                    wxappUserMapper.updateByIdSelective(user);
                }
            }
        }


        WxappLoginLog loginLog = new WxappLoginLog();
        loginLog.setUserCode(user.getUserCode());
        loginLog.setOpenId(user.getOpenId());
        loginLog.setLoginIp(IpHelper.getOriginIp(httpReq));
        wxappLoginLogMapper.insert(loginLog);

        // 基础信息验证完了后，进入统一的创建session的过程
        ssoFacadeContract.logout();
        return wxappLoginService.login(user);
    }

    public WxMaUserInfoResp miniappUserInfoResp() {
        WxappUser user = miniappUserInfo();
        String mobile = user.getMobile();
        if (StringUtils.isNotBlank(mobile)) {
            mobile = maskByRegular(mobile, "(?<=^.{3}).{4}");
        }
        WxMaUserInfoResp resp = new WxMaUserInfoResp();
        resp.setUserCode(user.getUserCode());
        resp.setNickname(user.getNickname());
        resp.setAvatar(fileosSignApi.sign(user.getAvatar()));
        resp.setOpenId(user.getOpenId());
        resp.setMobile(mobile);
        return resp;
    }

    public WxappUser miniappUserInfo() {
        String userCode = PrincipalContext.getUserCode();
        return wxappUserMapper.getWxappUserByUserCode(userCode);
    }

    @Transactional(rollbackFor = Exception.class)
    public boolean miniappUserinfoUpdate(WxappUser wu) {
        String userCode = PrincipalContext.getUserCode();

        WxappUser user = wxappUserMapper.getWxappUserByUserCode(userCode);
        if (user == null) {
            throw ValidationException.of("用户信息不全，无法操作更新");
        }
        boolean userChangeFlag = false;

        if (StringUtils.isBlank(wu.getNickname()) || !wu.getNickname().equals(user.getNickname())){
            user.setNickname(wu.getNickname());
            userChangeFlag = true;
        }
        if (StringUtils.isBlank(wu.getAvatar()) || !wu.getAvatar().equals(user.getAvatar())){
            user.setAvatar(wu.getAvatar());
            userChangeFlag = true;
        }
        /*
        if (StringUtils.isBlank(wu.getCity()) || !wu.getCity().equals(user.getCity())){
            user.setCity(wu.getCity());
            userChangeFlag = true;
        }
        if (StringUtils.isBlank(wu.getProvince()) || !wu.getProvince().equals(user.getProvince())){
            user.setProvince(wu.getProvince());
            userChangeFlag = true;
        }
        if (StringUtils.isBlank(wu.getCountry()) || !wu.getCountry().equals(user.getCountry())){
            user.setCountry(wu.getCountry());
            userChangeFlag = true;
        }
        */

        if (userChangeFlag){
            wxappUserMapper.updateByIdSelective(user);
        }
        return true;
    }

    public void miniappMobileBind(WxMaMobileBindReq req) {
        String userCode = PrincipalContext.getUserCode();

        WxMaService wxMaService = configuration.getMaService(req.getAppId());
        WxMaPhoneNumberInfo phoneNumberInfo;
        try {
            phoneNumberInfo = wxMaService.getUserService().getPhoneNumber(req.getCode());
        } catch (WxErrorException e) {
            throw new RuntimeException(e);
        }
        String mobile = phoneNumberInfo.getPhoneNumber();
        log.info("user wxapp mobile: appId: {}, userCode: {}, mobile: {}", req.getAppId(), userCode, mobile);

        if (!RegularTool.isMobile(mobile)) {
            throw ValidationException.of("手机号格式错误");
        }
        WxappUser wu = new WxappUser();
        wu.setUserCode(userCode);
        wu = wxappUserService.selectOneByEntity(wu);
        wu.setMobile(mobile);
        wxappUserService.updateByIdSelective(wu);
    }

    public WxMaPhoneNumberInfo getPhoneNoInfo(WxMaPhoneReq req) {
        final WxMaService wxService = configuration.getMaService();

        if (!wxService.getUserService().checkUserInfo(req.getSessionKey(), req.getRawData(), req.getSignature())) {
            throw ValidationException.of("user check failed");
        }

        return wxService.getUserService().getPhoneNoInfo(req.getSessionKey(), req.getEncryptedData(), req.getIv());
    }

    private static String maskByRegular(String str, String regular) {
        List<String> rts = RegularTool.find(str, regular);
        for (String rt : rts) {
            str = RegularTool.replaceAll(str, regular, "*".repeat(rt.length()));
        }
        return str;
    }

    private static Integer getUserGenderFromWechat(WxMaUserInfo userInfo){
        String gender = userInfo.getGender();
        Integer casGender = 0;
        if ("1".equals(gender)){
            casGender = 1;
        }
        if ("2".equals(gender)){
            casGender = 2;
        }
        return casGender;
    }


}
