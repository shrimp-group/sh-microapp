package com.wkclz.micro.wxmp.handler.spi;

import me.chanjar.weixin.common.session.WxSessionManager;
import me.chanjar.weixin.mp.api.WxMpService;
import me.chanjar.weixin.mp.bean.message.WxMpXmlMessage;
import me.chanjar.weixin.mp.bean.message.WxMpXmlOutMessage;

import java.util.Map;

public interface MenuSpi {

    WxMpXmlOutMessage autoReply(WxMpXmlMessage wxMessage,
                            Map<String, Object> context,
                            WxMpService weixinService,
                            WxSessionManager sessionManager);

}
