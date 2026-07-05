package com.wkclz.micro.wxmp.rest.cust;

import com.wkclz.core.base.R;
import com.wkclz.micro.wxmp.bean.resp.WxmpLoginResp;
import com.wkclz.micro.wxmp.rest.Route;
import com.wkclz.micro.wxmp.service.WxmpLoginService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Tag(name = "3.公众号登录", description = "微信公众号OAuth2登录")
@RestController
@RequestMapping(Route.PREFIX)
@Validated
public class WxmpLoginRest {

    @Autowired
    private WxmpLoginService wxmpLoginService;

    @Operation(summary = "1.公众号-OAuth2登录", description = "微信公众号OAuth2授权登录")
    @GetMapping(Route.PUBLIC_WXMP_LOGIN_APPID)
    public R<WxmpLoginResp> publicWxmpLoginAppid(
            @PathVariable("appid") String appid,
            @RequestParam("code") String code,
            HttpServletRequest req) {
        WxmpLoginResp resp = wxmpLoginService.login(appid, code, req);
        return R.ok(resp);
    }
}
