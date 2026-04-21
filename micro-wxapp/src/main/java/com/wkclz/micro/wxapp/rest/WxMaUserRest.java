package com.wkclz.micro.wxapp.rest;

import cn.binarywang.wx.miniapp.api.WxMaService;
import cn.binarywang.wx.miniapp.bean.WxMaPhoneNumberInfo;
import com.wkclz.core.base.R;
import com.wkclz.core.base.UserInfo;
import com.wkclz.core.exception.ValidationException;
import com.wkclz.iam.sdk.helper.SessionHelper;
import com.wkclz.iam.sdk.model.LoginResponse;
import com.wkclz.micro.file.api.FileApi;
import com.wkclz.micro.wxapp.Route;
import com.wkclz.micro.wxapp.bean.entity.WxappUser;
import com.wkclz.micro.wxapp.bean.vo.WxMaAppUserLoginVo;
import com.wkclz.micro.wxapp.config.WxMaConfiguration;
import com.wkclz.micro.wxapp.helper.Checker;
import com.wkclz.micro.wxapp.service.WxappUserService;
import com.wkclz.micro.wxapp.service.custom.WxMiniappService;
import com.wkclz.tool.tools.RegularTool;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import me.chanjar.weixin.common.error.WxErrorException;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 微信小程序用户接口
 *
 * @author <a href="https://github.com/binarywang">Binary Wang</a>
 */
@Slf4j
@RestController
@RequestMapping(Route.PREFIX)
public class WxMaUserRest {


    @Resource
    private FileApi fileApi;
    @Resource
    private WxMaConfiguration configuration;
    @Resource
    private WxMiniappService wxMiniappService;
    @Resource
    private WxappUserService wxappUserService;




    /**
     * @api {post} /public/miniapp/login 1. 小程序登录
     * @apiGroup MINIAPP
     * @apiVersion 0.0.1
     * @apiDescription code和appid必传,其他4个参数,要么全传要么一个都不能传,若不传,需要使用另外的接口上传用户信息。若只传两个信息,将只返回token
     *
     * @apiParam {String} code <code>body</code>code
     * @apiParam {String} appid <code>body</code>appid
     * @apiParam {String} [iv] <code>body</code>iv
     * @apiParam {String} [rawData] <code>body</code>rawData
     * @apiParam {String} [signature] <code>body</code>signature
     * @apiParam {String} [encryptedData] <code>body</code>encryptedData
     *
     * @apiParamExample {json} 请求样例:
     * {
     *     "code":"cccccccccc",
     *     "appid":"aaaaaaaaaa",
     *     "iv":"cccccccccc",
     *     "rawData":"cccccccccc",
     *     "signature":"cccccccccc",
     *     "encryptedData":"cccccccccc",
     * }
     *
     *
     * @apiSuccess {String} status <code>body</code>登录状态【0为成功】
     * @apiSuccess {String} token <code>body</code>登录成功时的token
     * @apiSuccess {String} msg <code>body</code>登录提示信息
     *
     * @apiSuccessExample {json} 请求样例:
     * {
     *   "code" : 1,
     *   "msg" : "success",
     *   "requestTime" : "2023-03-18 19:50:55",
     *   "responseTime" : "2023-03-18 19:50:56",
     *   "costTime" : 209,
     *   "data" : {
     *     "status" : 0,
     *     "token" : "xxxx.xxxx.xxxx",
     *     "msg" : "登录成功"
     *   },
     *   "success" : true
     * }
     *
     */
    @PostMapping(Route.MINIAPP_LOGIN)
    public R customerMiniappLogin(@RequestBody WxMaAppUserLoginVo vo, HttpServletRequest request) {
        Assert.notNull(vo.getAppId(), "appId 不能为空");
        Assert.notNull(vo.getCode(), "code 不能为空");

        if (!Checker.isValidWxAppId(vo.getAppId())) {
            throw ValidationException.of("appId 格式错误!");
        }

        LoginResponse response = wxMiniappService.miniappLogin(vo, request);
        return R.ok(response);
    }


    /**
     * @api {get} /miniapp/userinfo 2. 小程序用户信息
     * @apiGroup MINIAPP
     * @apiVersion 0.0.1
     * @apiDescription 获取小程序用户信息
     *
     * @apiSuccess {String} userCode <code>body</code>用户编码
     * @apiSuccess {String} nickname <code>body</code>昵称,未修改时为用户名
     * @apiSuccess {String} avatar <code>body</code>头像
     * @apiSuccess {String} gender <code>body</code>性别【不是微信的编码】:0女1男2隐藏
     *
     * @apiSuccessExample {json} 请求样例:
     * {
     *   "code" : 1,
     *   "msg" : "success",
     *   "data" : {
     *     "userCode" : "用户编码",
     *     "nickname" : "昵称",
     *     "avatar" : "头像",
     *     "gender" : "性别"
     *   }
     * }
     *
     */
    @GetMapping(Route.MINIAPP_USERINFO)
    public R customerMiniappUserinfo() {
        WxappUser user = wxMiniappService.miniappUserInfo();
        String mobile = user.getMobile();
        if (StringUtils.isNotBlank(mobile)) {
            mobile = maskByRegular(mobile, "(?<=^.{3}).{4}");
        }
        UserInfo ui = new UserInfo();
        ui.setUserCode(user.getUserCode());
        ui.setNickname(user.getNickname());
        ui.setAvatar(fileApi.sign(user.getAvatar()));
        ui.setOpenId(user.getOpenId());
        ui.setMobile(mobile);
        return R.ok(ui);
    }


    /**
     * @api {post} /miniapp/userinfo/update 3. 小程序更新用户信息
     * @apiGroup MINIAPP
     * @apiVersion 0.0.1
     * @apiDescription 更新用户信息
     *
     * @apiParam {String} [nickname] <code>body</code>昵称
     * @apiParam {String} [gender] <code>body</code>性别【使用微信的编码】
     * @apiParam {String} [language] <code>body</code>语言
     * @apiParam {String} [city] <code>body</code>city城市
     * @apiParam {String} [province] <code>body</code>province省
     * @apiParam {String} [country] <code>body</code>country区
     * @apiParam {String} [avatarUrl] <code>body</code>avatarUrl头像地址
     * @apiParam {String} [unionId] <code>body</code>unionId
     *
     * @apiParamExample {json} 请求样例:
     * {
     *     "nickname":"cccccccccc",
     *     "gender":"cccccccccc",
     *     "city":"cccccccccc",
     *     "avatarUrl":"cccccccccc",
     * }
     *
     */
    @PostMapping(Route.MINIAPP_USERINFO_UPDATE)
    public R customerMiniappUserinfoUpdate(@RequestBody WxappUser userInfo) {
        if (StringUtils.isBlank(userInfo.getNickname()) && StringUtils.isBlank(userInfo.getAvatar())) {
            throw ValidationException.of("没有头像也没有昵称！");
        }
        boolean b = wxMiniappService.miniappUserinfoUpdate( userInfo);
        return R.ok(b);
    }


    /**
     * @api {post} /miniapp/mobile/bind 4. 小程序内绑定手机
     * @apiGroup MINIAPP
     * @apiVersion 0.0.1
     *
     * @apiParam {String} code 用于获取手机号的Code
     *
     * @apiParamExample {json} 请求样例:
     * {
     *     "code": "xxxxxxxxxxxxxxxx"
     * }
     *
     * @apiSuccessExample {json} 返回样例:
     * {
     *      "code": 1,
     *      "data": true
     * }
     *
     */
    @PostMapping(Route.MINIAPP_MOBILE_BIND)
    public R miniappMobileBind(@RequestBody Map<String, String> param) throws WxErrorException {
        String code = param.get("code");
        String appId = param.get("appId");
        if (StringUtils.isBlank(code)) {
            return R.error("code can not be null");
        }
        if (StringUtils.isBlank(appId)) {
            return R.error("appId can not be null");
        }
        String userCode = SessionHelper.getUserCode();

        WxMaService wxMaService = configuration.getMaService(appId);
        WxMaPhoneNumberInfo phoneNumberInfo = wxMaService.getUserService().getPhoneNumber(code);
        String mobile = phoneNumberInfo.getPhoneNumber();
        log.info("user wxapp mobile: appId: {}, userCode: {}, mobile: {}", appId, userCode, mobile);

        if (!RegularTool.isMobile(mobile)) {
            return R.error("手机号格式错误");
        }
        WxappUser wu = new WxappUser();
        wu.setUserCode(SessionHelper.getUserCode());
        wu = wxappUserService.selectOneByEntity(wu);
        wu.setMobile(mobile);
        wxappUserService.updateByIdSelective(wu);
        return R.ok();
    }


    /**
     * @api {get} /customer/wx/user/phone 5. customer-微信用户手机号
     * @apiGroup WX
     *
     * @apiVersion 0.0.1
     *
     * @apiParamExample {json} 请求样例:
     * /cuntomer/wx/user/phone
     *
     * @apiSuccess {String} phoneNumber phoneNumber
     * @apiSuccess {String} purePhoneNumber purePhoneNumber
     * @apiSuccess {String} countryCode countryCode
     * @apiSuccess {String} watermark watermark
     *
     * @apiSuccessExample {json} 返回样例:
     * {
     *      "code": 1,
     *      "data": {
     *          "phoneNumber": "phoneNumber",
     *          "purePhoneNumber": "purePhoneNumber",
     *          "countryCode": "countryCode",
     *          "watermark": "watermark",
     *      }
     * }
     *
     */
    @GetMapping(Route.CUSTOMER_WX_USER_PHONE)
    public R phone(String sessionKey, String signature, String rawData, String encryptedData, String iv) {
        final WxMaService wxService = configuration.getMaService();

        // 用户信息校验
        if (!wxService.getUserService().checkUserInfo(sessionKey, rawData, signature)) {
            throw ValidationException.of("user check failed");
        }

        // 解密
        WxMaPhoneNumberInfo phoneNoInfo = wxService.getUserService().getPhoneNoInfo(sessionKey, encryptedData, iv);
        return R.ok(phoneNoInfo);
    }


    private static String maskByRegular(String str, String regular) {
        List<String> rts = RegularTool.find(str, regular);
        for (String rt : rts) {
            str = RegularTool.replaceAll(str, regular, "*".repeat(rt.length()));
        }
        return str;
    }

}
