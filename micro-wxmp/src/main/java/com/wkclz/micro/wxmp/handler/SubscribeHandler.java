package com.wkclz.micro.wxmp.handler;

import com.wkclz.micro.wxmp.handler.spi.SubscribeSpi;
import com.wkclz.micro.wxmp.pojo.entity.WxmpUser;
import com.wkclz.micro.wxmp.service.WxmpUserService;
import jakarta.annotation.Resource;
import me.chanjar.weixin.common.error.WxErrorException;
import me.chanjar.weixin.common.session.WxSessionManager;
import me.chanjar.weixin.mp.api.WxMpService;
import me.chanjar.weixin.mp.bean.message.WxMpXmlMessage;
import me.chanjar.weixin.mp.bean.message.WxMpXmlOutMessage;
import me.chanjar.weixin.mp.bean.result.WxMpUser;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * @author <a href="https://github.com/binarywang">Binary Wang</a>
 */
@Component
public class SubscribeHandler extends AbstractHandler {


    @Autowired(required = false)
    private SubscribeSpi subscribeSpi;
    @Resource
    private WxmpUserService wxmpUserService;


    @Override
    public WxMpXmlOutMessage handle(WxMpXmlMessage wxMessage,
                                    Map<String, Object> context,
                                    WxMpService weixinService,
                                    WxSessionManager sessionManager) throws WxErrorException {

        // wxMessage.getAuthorizeAppId() 这里的 appId 为空
        String appId = weixinService.getWxMpConfigStorage().getAppId();
        this.logger.info("新关注用户 appId: {}, openId: {}", appId, wxMessage.getFromUser());

        // 获取微信用户基本信息
        try {
            WxMpUser userWxInfo = weixinService.getUserService().userInfo(wxMessage.getFromUser(), null);
            if (userWxInfo != null) {

                String openId = userWxInfo.getOpenId();
                String unionId = userWxInfo.getUnionId();
                String[] privileges = userWxInfo.getPrivileges();

                WxmpUser user = new WxmpUser();
                user.setAppId(appId);
                user.setOpenId(openId);
                user.setUnionId(unionId);

                if (privileges != null && privileges.length > 0) {
                    user.setPrivilegeList(StringUtils.join(privileges, ","));
                }
                wxmpUserService.userSubscribe(user);
            }
        } catch (WxErrorException e) {
            if (e.getError().getErrorCode() == 48001) {
                this.logger.info("该公众号没有获取用户信息权限！");
                return null;
            }
        }

        if (subscribeSpi == null) {
            return null;
        }

        try {
            WxMpXmlOutMessage message = subscribeSpi.autoReply(wxMessage, context, weixinService, sessionManager);
            logger.info("关注公众号, 回复消息: message: {}", message);
            return message;
        } catch (Exception e) {
            logger.error("执行 spi 失败: {}, {}", subscribeSpi.getClass().getName(), e.getMessage());
        }

        /*

        WxMpXmlOutMessage responseResult = null;
        try {
            responseResult = this.handleSpecial(wxMessage);
        } catch (Exception e) {
            this.logger.error(e.getMessage(), e);
        }

        if (responseResult != null) {
            return responseResult;
        }

        try {
            WxmpConfig config = wxmpConfigService.getConfigByAppId(appId);
            String welcomeMsg = config.getWelcomeMsg();
            if (StringUtils.isNotBlank(welcomeMsg)) {

                return new TextBuilder().build(welcomeMsg, wxMessage, weixinService);
            }
            return new TextBuilder().build("感谢关注", wxMessage, weixinService);
        } catch (Exception e) {
            this.logger.error(e.getMessage(), e);
        }
        */
        return null;
    }

    /**
     * 处理特殊请求，比如如果是扫码进来的，可以做相应处理
     */
    private WxMpXmlOutMessage handleSpecial(WxMpXmlMessage wxMessage)
        throws Exception {
        //TODO
        return null;
    }

}
