package com.wkclz.micro.wxapp.rest;

import cn.binarywang.wx.miniapp.api.WxMaService;
import com.wkclz.micro.wxapp.Route;
import com.wkclz.micro.wxapp.config.WxMaConfiguration;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
public class WxAppRest {


    @Autowired
    private WxMaConfiguration configuration;

    /**
     * @api {get} /public/miniapp/portal 1. customer-微信验签[暂时废弃]
     * @apiGroup WX
     *
     * @apiVersion 0.0.1
     *
     * @apiParamExample {json} 请求样例:
     * /customer/wx/portal
     *
     * @apiSuccessExample {json} 返回样例:
     * vvvvvvvvvvvvvv
     */
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
