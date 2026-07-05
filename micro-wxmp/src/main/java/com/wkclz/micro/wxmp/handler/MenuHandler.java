package com.wkclz.micro.wxmp.handler;

import com.wkclz.micro.wxmp.handler.spi.MenuSpi;
import me.chanjar.weixin.common.session.WxSessionManager;
import me.chanjar.weixin.mp.api.WxMpService;
import me.chanjar.weixin.mp.bean.message.WxMpXmlMessage;
import me.chanjar.weixin.mp.bean.message.WxMpXmlOutMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

import static me.chanjar.weixin.common.api.WxConsts.EventType;

/**
 * @author <a href="https://github.com/binarywang">Binary Wang</a>
 */
@Component
public class MenuHandler extends AbstractHandler {

    @Autowired(required = false)
    private MenuSpi menuSpi;

    @Override
    public WxMpXmlOutMessage handle(WxMpXmlMessage wxMessage,
                                    Map<String, Object> context,
                                    WxMpService weixinService,
                                    WxSessionManager sessionManager) {
        logger.info("菜单点击: wxMessage: {}, context: {}", wxMessage, context);

        if (EventType.VIEW.equals(wxMessage.getEvent())) {
            return null;
        }

        if (menuSpi == null) {
            return null;
        }

        try {
            WxMpXmlOutMessage message = menuSpi.autoReply(wxMessage, context, weixinService, sessionManager);
            logger.info("菜单点击, 发送消息: message: {}", message);
            return message;
        } catch (Exception e) {
            logger.error("执行 spi 失败: {}, {}", menuSpi.getClass().getName(), e.getMessage());
        }

        return null;
        /*
        return WxMpXmlOutMessage.TEXT().content(msg)
            .fromUser(wxMessage.getToUser()).toUser(wxMessage.getFromUser())
            .build();
        */
    }

}
