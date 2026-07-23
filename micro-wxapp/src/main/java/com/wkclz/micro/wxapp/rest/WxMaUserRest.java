package com.wkclz.micro.wxapp.rest;

import cn.binarywang.wx.miniapp.bean.WxMaPhoneNumberInfo;
import com.wkclz.core.base.PageData;
import com.wkclz.core.base.R;
import com.wkclz.core.identity.IdentityContext;
import com.wkclz.iam.session.bean.resp.LoginResp;
import com.wkclz.micro.wxapp.Route;
import com.wkclz.micro.wxapp.bean.entity.WxappUser;
import com.wkclz.micro.wxapp.bean.req.*;
import com.wkclz.micro.wxapp.bean.resp.WxMaUserInfoResp;
import com.wkclz.micro.wxapp.bean.resp.WxappUserPageResp;
import com.wkclz.micro.wxapp.service.WxappUserService;
import com.wkclz.micro.wxapp.service.custom.WxMiniappService;
import com.wkclz.tool.utils.BeanUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Tag(name = "小程序用户", description = "微信小程序用户管理接口")
@Slf4j
@RestController
@RequestMapping(Route.PREFIX)
@Validated
public class WxMaUserRest {

    @Resource
    private WxMiniappService wxMiniappService;

    @Resource
    private WxappUserService wxappUserService;

    @Operation(summary = "1. 小程序登录")
    @PostMapping(Route.MINIAPP_LOGIN)
    public R<LoginResp> customerMiniappLogin(@Valid @RequestBody WxMaLoginReq req, HttpServletRequest request) {
        LoginResp resp = wxMiniappService.miniappLogin(req, request);
        return R.ok(resp);
    }

    @Operation(summary = "2. 小程序用户信息")
    @GetMapping(Route.MINIAPP_USERINFO)
    public R<WxMaUserInfoResp> customerMiniappUserinfo() {
        WxMaUserInfoResp resp = wxMiniappService.miniappUserInfoResp();
        return R.ok(resp);
    }

    @Operation(summary = "3. 小程序更新用户信息")
    @PostMapping(Route.MINIAPP_USERINFO_UPDATE)
    public R<Boolean> customerMiniappUserinfoUpdate(@Valid @RequestBody WxMaUserInfoUpdateReq req) {
        WxappUser userInfo = BeanUtil.cp(req, WxappUser.class);
        boolean b = wxMiniappService.miniappUserinfoUpdate(userInfo);
        return R.ok(b);
    }

    @Operation(summary = "4. 小程序绑定手机")
    @PostMapping(Route.MINIAPP_MOBILE_BIND)
    public R<Void> miniappMobileBind(@Valid @RequestBody WxMaMobileBindReq req) {
        wxMiniappService.miniappMobileBind(req);
        return R.ok();
    }

    @Operation(summary = "5. 获取手机号")
    @GetMapping(Route.CUSTOMER_WX_USER_PHONE)
    public R<WxMaPhoneNumberInfo> phone(@Valid WxMaPhoneReq req) {
        WxMaPhoneNumberInfo phoneNoInfo = wxMiniappService.getPhoneNoInfo(req);
        return R.ok(phoneNoInfo);
    }

    @Operation(summary = "6. 微信小程序-用户-分页查询")
    @GetMapping(Route.WXAPP_USER_PAGE)
    public R<PageData<WxappUserPageResp>> wxappUserPage(@Valid WxappUserPageReq req) {
        WxappUser entity = BeanUtil.cp(req, WxappUser.class);
        entity.setTenantCode(IdentityContext.getTenantCode());
        PageData<WxappUser> page = wxappUserService.getUserPage(entity);
        PageData<WxappUserPageResp> newPage = page.convert(WxappUserPageResp.class);
        return R.ok(newPage);
    }
}
