package com.wkclz.micro.wxapp.service.custom;

import cn.binarywang.wx.miniapp.api.WxMaService;
import cn.binarywang.wx.miniapp.bean.WxMaJscode2SessionResult;
import cn.binarywang.wx.miniapp.bean.WxMaUserInfo;
import com.wkclz.auth.sdk.helper.AuthHelper;
import com.wkclz.auth.sdk.pojo.LoginResponse;
import com.wkclz.common.exception.BizException;
import com.wkclz.common.utils.AssertUtil;
import com.wkclz.micro.wxapp.config.WxMaConfiguration;
import com.wkclz.micro.wxapp.dao.WxappLoginLogMapper;
import com.wkclz.micro.wxapp.dao.WxappUserMapper;
import com.wkclz.micro.wxapp.bean.entity.WxappLoginLog;
import com.wkclz.micro.wxapp.bean.entity.WxappUser;
import com.wkclz.micro.wxapp.bean.vo.WxMaAppUserLoginVo;
import com.wkclz.redis.gen.RedisIdGenHelper;
import com.wkclz.spring.helper.IpHelper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import me.chanjar.weixin.common.error.WxErrorException;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestBody;

@Service
@AllArgsConstructor
public class WxMiniappService {

    private static final Logger logger = LoggerFactory.getLogger(WxMiniappService.class);

    private final AuthHelper authHelper;
    private final WxMaConfiguration configuration;
    private final WxappUserMapper wxappUserMapper;
    private final RedisIdGenHelper redisIdGenHelper;
    private final WxappLoginService wxappLoginService;
    private final WxappLoginLogMapper wxappLoginLogMapper;;

    @Transactional(rollbackFor = Exception.class)
    public LoginResponse miniappLogin(@RequestBody WxMaAppUserLoginVo vo, HttpServletRequest req) {

        // 用户信息选填，若只要传了其中一个，其他都必填，
        int withUserInfo = 0;
        if (StringUtils.isNotBlank(vo.getEncryptedData())) {
            withUserInfo++;
        }
        if (StringUtils.isNotBlank(vo.getIv())) {
            withUserInfo++;
        }
        if (StringUtils.isNotBlank(vo.getRawData())) {
            withUserInfo++;
        }
        if (StringUtils.isNotBlank(vo.getSignature())) {
            withUserInfo++;
        }
        if (withUserInfo > 0 && withUserInfo != 4) {
            AssertUtil.notNull(vo.getEncryptedData(), "encryptedData 不能为空");
            AssertUtil.notNull(vo.getIv(), "iv 不能为空");
            AssertUtil.notNull(vo.getRawData(), "rawData 不能为空");
            AssertUtil.notNull(vo.getSignature(), "signature 不能为空");
        }

        WxMaService wxService = configuration.getMaService(vo.getAppId());
        WxMaJscode2SessionResult session = null;
        try {
            session = wxService.getUserService().getSessionInfo(vo.getCode());
        } catch (WxErrorException e) {
            throw new RuntimeException(e);
        }

        String openid = session.getOpenid();
        String unionid = session.getUnionid();
        String sessionKey = session.getSessionKey();
        logger.info("openid:{}, unionid:{}, sessionKey:{}", openid, unionid, sessionKey);

        WxMaUserInfo wxMaUserInfo = null;
        if (withUserInfo == 4) {
            // 用户信息校验
            if (!wxService.getUserService().checkUserInfo(sessionKey, vo.getRawData(), vo.getSignature())) {
                throw BizException.error("Wxapp check failed");
            }
            // 解密用户信息
            wxMaUserInfo = wxService.getUserService().getUserInfo(sessionKey, vo.getEncryptedData(), vo.getIv());
        }

        WxappUser user = wxappUserMapper.getWxappUserByOpenId(openid);
        // 第一次登录，什么信息都没有
        if (user == null) {
            String userCode = wxMaUserInfo == null ? "wxapp_" + redisIdGenHelper.nextShot() : wxMaUserInfo.getNickName();

            user = new WxappUser();
            user.setAppId(vo.getAppId());
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

            wxappUserMapper.insert(user);
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
                    wxappUserMapper.updateSelective(user);
                }
            }
        }


        WxappLoginLog log = new WxappLoginLog();
        log.setUserCode(user.getUserCode());
        log.setOpenId(user.getOpenId());
        log.setLoginIp(IpHelper.getOriginIp(req));
        wxappLoginLogMapper.insert(log);

        // 基础信息验证完了后，进入统一的创建session的过程
        authHelper.invalidToken();
        return wxappLoginService.login(user);
    }

    public WxappUser miniappUserInfo() {
        String userCode = authHelper.getUserCode();
        return wxappUserMapper.getWxappUserByUserCode(userCode);
    }

    @Transactional(rollbackFor = Exception.class)
    public boolean miniappUserinfoUpdate(WxappUser wu) {
        String userCode = authHelper.getUserCode();

        WxappUser user = wxappUserMapper.getWxappUserByUserCode(userCode);
        if (user == null) {
            throw BizException.error("用户信息不全，无法操作更新");
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
            wxappUserMapper.updateSelective(user);
        }
        return true;
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
