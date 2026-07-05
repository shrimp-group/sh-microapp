package com.wkclz.micro.wxapp.rest;

import cn.binarywang.wx.miniapp.api.WxMaService;
import com.wkclz.micro.wxapp.Route;
import com.wkclz.micro.wxapp.config.WxMaConfiguration;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "微信小程序验签", description = "微信小程序服务器验签接口")
@Slf4j
@RestController
@RequestMapping(Route.PREFIX)
@Validated
public class WxAppRest {


    @Autowired
    private WxMaConfiguration configuration;

    @Operation(summary = "1. 微信小程序验签")
    @GetMapping(value = Route.CUSTOMER_WX_PORTAL, produces = "text/plain;charset=utf-8")
    public String authGet(@RequestParam(name = "signature", required = false) String signature,
                          @RequestParam(name = "timestamp", required = false) String timestamp,
                          @RequestParam(name = "nonce", required = false) String nonce,
                          @RequestParam(name = "echostr", required = false) String echostr) {
        log.info("\n接收到来自微信服务器的认证消息：signature = [{}], timestamp = [{}], nonce = [{}], echostr = [{}]", signature, timestamp, nonce, echostr);
        if (StringUtils.isAnyBlank(signature, timestamp, nonce, echostr)) {
            throw new IllegalArgumentException("请求参数非法，请核实!");
        }
        final WxMaService wxService = configuration.getMaService();
        if (wxService.checkSignature(timestamp, nonce, signature)) {
            return echostr;
        }
        return "非法请求";
    }



}
