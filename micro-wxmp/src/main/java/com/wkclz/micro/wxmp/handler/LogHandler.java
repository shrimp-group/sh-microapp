package com.wkclz.micro.wxmp.handler;

import com.wkclz.micro.wxmp.handler.spi.LogSpi;
import me.chanjar.weixin.common.session.WxSessionManager;
import me.chanjar.weixin.mp.api.WxMpService;
import me.chanjar.weixin.mp.bean.message.WxMpXmlMessage;
import me.chanjar.weixin.mp.bean.message.WxMpXmlOutMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * @author <a href="https://github.com/binarywang">Binary Wang</a>
 */
@Component
public class LogHandler extends AbstractHandler {


    @Autowired(required = false)
    private LogSpi logSpi;

    @Override
    public WxMpXmlOutMessage handle(
        WxMpXmlMessage wxMessage,
        Map<String, Object> context,
        WxMpService wxMpService,
        WxSessionManager sessionManager) {
        // this.logger.info("接收到请求消息，内容：{}", JSON.toJSONString(wxMessage));


        if (logSpi == null) {
            return null;
        }

        try {
            return logSpi.autoReply(wxMessage, context, wxMpService, sessionManager);
        } catch (Exception e) {
            logger.error("执行 spi 失败: {}, {}", logSpi.getClass().getName(), e.getMessage());
        }

        return null;
    }

}
