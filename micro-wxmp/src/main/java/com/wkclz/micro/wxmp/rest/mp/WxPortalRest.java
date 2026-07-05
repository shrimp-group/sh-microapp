package com.wkclz.micro.wxmp.rest.mp;

import com.wkclz.micro.wxmp.config.WxMpConfiguration;
import com.wkclz.micro.wxmp.rest.Route;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import me.chanjar.weixin.mp.api.WxMpMessageRouter;
import me.chanjar.weixin.mp.api.WxMpService;
import me.chanjar.weixin.mp.bean.message.WxMpXmlMessage;
import me.chanjar.weixin.mp.bean.message.WxMpXmlOutMessage;
import org.apache.commons.lang3.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * @author <a href="https://github.com/binarywang">Binary Wang</a>
 */
@Tag(name = "6.消息回调", description = "微信公众号消息回调入口")
@Slf4j
@RestController
@RequestMapping(Route.PREFIX)
@Validated
public class WxPortalRest {

    @Resource
    private WxMpConfiguration wxMpConfiguration;

    @Operation(summary = "1.消息回调-验签", description = "微信服务器验证签名")
    @GetMapping(value = Route.PUBLIC_WXMP_PORTAL_APPID, produces = "text/plain;charset=utf-8")
    public String wxmpPortalAppidGet(
                @PathVariable String appid,
                @RequestParam(name = "signature", required = false) String signature,
                @RequestParam(name = "timestamp", required = false) String timestamp,
                @RequestParam(name = "nonce", required = false) String nonce,
                @RequestParam(name = "echostr", required = false) String echostr) {

        log.info("接收到来自微信服务器的认证消息：[{}, {}, {}, {}, {}]", appid, signature, timestamp, nonce, echostr);
        if (StringUtils.isAnyBlank(signature, timestamp, nonce, echostr)) {
            log.error("{} 请求参数非法，请核实", appid);
            throw new IllegalArgumentException("请求参数非法，请核实!");
        }
        WxMpService mpService = wxMpConfiguration.getMpService(appid);
        if (mpService == null) {
            log.error("未找到对应appid=[{}]的配置，请核实！", appid);
            return String.format("未找到对应appid=[%s]的配置，请核实！", appid);
        }
        if (mpService.checkSignature(timestamp, nonce, signature)) {
            log.info("{} 验证成功，返回随机字符串：{}", appid, echostr);
            return echostr;
        }
        log.error("{} 验证失败，随机字符串：{}", appid, echostr);
        return "非法请求";
    }

    @Operation(summary = "2.消息回调-消息路由", description = "接收微信消息并路由到对应处理器")
    @PostMapping(value = Route.PUBLIC_WXMP_PORTAL_APPID, produces = "text/plain;charset=utf-8")
    public String wxmpPortalAppidPost(
                @PathVariable String appid,
                @RequestBody String requestBody,
                @RequestParam("nonce") String nonce,
                @RequestParam("openid") String openid,
                @RequestParam("signature") String signature,
                @RequestParam("timestamp") String timestamp,
                @RequestParam(name = "encrypt_type", required = false) String encType,
                @RequestParam(name = "msg_signature", required = false) String msgSignature) {

        log.info("接收微信请求：[appid=[{}], openid=[{}], [signature=[{}], encType=[{}], msgSignature=[{}],"
                + " timestamp=[{}], nonce=[{}], requestBody=[\n{}\n] ",
            appid, openid, signature, encType, msgSignature, timestamp, nonce, requestBody);

        WxMpService mpService = wxMpConfiguration.getMpService(appid);
        if (mpService == null) {
            return String.format("未找到对应appid=[%s]的配置，请核实！", appid);
        }
        if (!mpService.checkSignature(timestamp, nonce, signature)) {
            throw new IllegalArgumentException("非法请求，可能属于伪造的请求！");
        }
        String out = null;
        if (StringUtils.isBlank(encType)) {
            // 明文传输的消息
            WxMpXmlMessage inMessage = WxMpXmlMessage.fromXml(requestBody);
            WxMpXmlOutMessage outMessage = this.route(inMessage, appid);
            if (outMessage == null) {
                return "";
            }
            out = outMessage.toXml();
        } else if ("aes".equalsIgnoreCase(encType)) {
            // aes加密的消息
            WxMpXmlMessage inMessage = WxMpXmlMessage.fromEncryptedXml(requestBody, mpService.getWxMpConfigStorage(),
                timestamp, nonce, msgSignature);
            log.debug("\n消息解密后内容为：\n{} ", inMessage.toString());
            WxMpXmlOutMessage outMessage = this.route(inMessage, appid);
            if (outMessage == null) {
                return "";
            }
            out = outMessage.toEncryptedXml(mpService.getWxMpConfigStorage());
        } else {
            log.error("未知的消息加密方式!");
        }

        log.debug("\n组装回复信息：{}", out);
        return out;
    }

    private WxMpXmlOutMessage route(WxMpXmlMessage message, String appid) {
        try {
            WxMpMessageRouter messageRouter = wxMpConfiguration.messageRouter(appid);
            return messageRouter.route(message);
        } catch (Exception e) {
            log.error("路由消息时出现异常！", e);
        }

        return null;
    }
}
