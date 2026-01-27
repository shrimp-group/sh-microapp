package com.wkclz.micro.wxmp.handler;

import com.alibaba.fastjson2.JSON;
import com.wkclz.micro.wxmp.builder.TextBuilder;
import com.wkclz.micro.wxmp.handler.spi.MsgSpi;
import com.wkclz.micro.wxmp.pojo.entity.WxmpKfMsg;
import com.wkclz.micro.wxmp.service.WxmpKfMsgService;
import jakarta.annotation.Resource;
import me.chanjar.weixin.common.session.WxSessionManager;
import me.chanjar.weixin.mp.api.WxMpService;
import me.chanjar.weixin.mp.bean.message.WxMpXmlMessage;
import me.chanjar.weixin.mp.bean.message.WxMpXmlOutMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.Map;

import static me.chanjar.weixin.common.api.WxConsts.XmlMsgType;

/**
 * @author <a href="https://github.com/binarywang">Binary Wang</a>
 */
@Component
public class MsgHandler extends AbstractHandler {

    @Autowired(required = false)
    private MsgSpi msgSpi;
    @Resource
    private WxmpKfMsgService wxmpKfMsgService;

    @Override
    public WxMpXmlOutMessage handle(WxMpXmlMessage wxMessage,
                                    Map<String, Object> context,
                                    WxMpService weixinService,
                                    WxSessionManager sessionManager) {

        String appId = weixinService.getWxMpConfigStorage().getAppId();
        logger.info("客服消息: wxMessage: {}, context: {}", wxMessage, context);

        if (!wxMessage.getMsgType().equals(XmlMsgType.EVENT)) {
            WxmpKfMsg msg = new WxmpKfMsg();
            msg.setAppId(appId);
            msg.setMsgType(wxMessage.getMsgType());
            msg.setMsgId(wxMessage.getMsgId());
            msg.setFromUser(wxMessage.getFromUser());
            msg.setToUser(wxMessage.getToUser());
            msg.setContent(wxMessage.getContent());
            msg.setMsgTime(new Date(wxMessage.getCreateTime()));
            try {
                wxmpKfMsgService.insert(msg);
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
        }

        //当用户输入关键词如“你好”，“客服”等，并且有客服在线时，把消息转发给在线客服
        /*
        try {
            if (StringUtils.startsWithAny(wxMessage.getContent(), "你好", "客服")
                && weixinService.getKefuService().kfOnlineList()
                .getKfOnlineList().size() > 0) {
                return WxMpXmlOutMessage.TRANSFER_CUSTOMER_SERVICE()
                    .fromUser(wxMessage.getToUser())
                    .toUser(wxMessage.getFromUser()).build();
            }
        } catch (WxErrorException e) {
            e.printStackTrace();
        }
        */

        if (msgSpi == null) {
            String content = "收到信息内容：" + JSON.toJSONString(wxMessage);
            return new TextBuilder().build(content, wxMessage, weixinService);
        }

        try {
            WxMpXmlOutMessage message = msgSpi.autoReply(wxMessage, context, weixinService, sessionManager);
            logger.info("收到客服消息, 回复消息: message: {}", message);
            return message;
        } catch (Exception e) {
            logger.error("执行 spi 失败: {}, {}", msgSpi.getClass().getName(), e.getMessage());
        }
        return null;
    }

}