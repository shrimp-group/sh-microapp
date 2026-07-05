package com.wkclz.micro.wxmp.rest.mp;

import com.wkclz.core.base.R;
import com.wkclz.micro.wxmp.config.WxMpConfiguration;
import com.wkclz.micro.wxmp.bean.req.WxSignReq;
import com.wkclz.micro.wxmp.rest.Route;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import me.chanjar.weixin.common.bean.WxJsapiSignature;
import me.chanjar.weixin.common.error.WxErrorException;
import me.chanjar.weixin.mp.api.WxMpService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author shrimp
 */
@Tag(name = "7.JSAPI签名", description = "微信公众号JSAPI签名")
@RestController
@RequestMapping(Route.PREFIX)
@Validated
public class WxSignRest {

    @Autowired
    private WxMpConfiguration wxMpConfiguration;

    @Operation(summary = "1.JSAPI签名", description = "获取微信公众号JSAPI签名")
    @GetMapping(Route.H5_WX_SIGN)
    public R<WxJsapiSignature> h5WxSign(@Valid WxSignReq req) throws WxErrorException {
        WxMpService mpService = wxMpConfiguration.getMpService(req.getAppId());
        WxJsapiSignature ws = mpService.createJsapiSignature(req.getUrl());
        return R.ok(ws);
    }
}
