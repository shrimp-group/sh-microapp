package com.wkclz.micro.wxmp.config;

import com.google.common.collect.Maps;
import com.wkclz.core.exception.ValidationException;
import com.wkclz.micro.wxmp.handler.*;
import com.wkclz.micro.wxmp.pojo.vo.WxMpAppInfo;
import com.wkclz.micro.wxmp.service.WxmpConfigService;
import lombok.AllArgsConstructor;
import me.chanjar.weixin.mp.api.WxMpMessageRouter;
import me.chanjar.weixin.mp.api.WxMpService;
import me.chanjar.weixin.mp.api.impl.WxMpServiceImpl;
import me.chanjar.weixin.mp.config.impl.WxMpDefaultConfigImpl;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.Assert;

import java.util.Map;

import static me.chanjar.weixin.common.api.WxConsts.EventType;
import static me.chanjar.weixin.common.api.WxConsts.EventType.SUBSCRIBE;
import static me.chanjar.weixin.common.api.WxConsts.EventType.UNSUBSCRIBE;
import static me.chanjar.weixin.common.api.WxConsts.XmlMsgType;
import static me.chanjar.weixin.common.api.WxConsts.XmlMsgType.EVENT;
import static me.chanjar.weixin.mp.constant.WxMpEventConstants.CustomerService.*;
import static me.chanjar.weixin.mp.constant.WxMpEventConstants.POI_CHECK_NOTIFY;

/**
 * wechat mp configuration
 *
 * @author <a href="https://github.com/binarywang">Binary Wang</a>
 */
@AllArgsConstructor
@Configuration
public class WxMpConfiguration {

    private final LogHandler logHandler;
    private final MsgHandler msgHandler;
    private final NullHandler nullHandler;
    private final MenuHandler menuHandler;
    private final ScanHandler scanHandler;
    private final LocationHandler locationHandler;
    private final SubscribeHandler subscribeHandler;
    private final KfSessionHandler kfSessionHandler;
    private final UnsubscribeHandler unsubscribeHandler;
    private final StoreCheckNotifyHandler storeCheckNotifyHandler;

    private final WxmpConfigService wxmpConfigService;

    private static final Map<String, WxMpService> MP_SERVICES = Maps.newHashMap();

    public WxMpService getMpService(String appid) {
        Assert.notNull(appid, "appid 不能为空");

        WxMpService mpService = MP_SERVICES.get(appid);
        if (mpService == null) {
            init(appid);
        }
        return MP_SERVICES.get(appid);
    }

    private synchronized void init(String appid) {

        WxMpAppInfo wxApp = new WxMpAppInfo();
        wxApp.setAppId(appid);
        wxApp = wxmpConfigService.getWxMpAppInfo(wxApp);
        if (wxApp == null) {
            throw ValidationException.of("没有找到公众号配置：appid: {}", appid);
        }
        WxMpService service = new WxMpServiceImpl();

        // default
        WxMpDefaultConfigImpl configStorage = new WxMpDefaultConfigImpl();
        configStorage.setAppId(wxApp.getAppId());
        configStorage.setSecret(wxApp.getAppSecret());
        configStorage.setToken(wxApp.getMpToken());
        configStorage.setAesKey(wxApp.getAesKey());

        service.setWxMpConfigStorage(configStorage);
        MP_SERVICES.put(appid, service);
    }

    public WxMpMessageRouter messageRouter(String appid) {
        WxMpService mpService = getMpService(appid);
        return messageRouter(mpService);

    }
    public WxMpMessageRouter messageRouter(WxMpService mpService) {
        final WxMpMessageRouter newRouter = new WxMpMessageRouter(mpService);

        // 记录所有事件的日志 （异步执行）
        newRouter.rule().handler(this.logHandler).next();

        // 接收客服会话管理事件
        newRouter.rule().async(false).msgType(EVENT).event(KF_CREATE_SESSION).handler(this.kfSessionHandler).end();
        newRouter.rule().async(false).msgType(EVENT).event(KF_CLOSE_SESSION).handler(this.kfSessionHandler).end();
        newRouter.rule().async(false).msgType(EVENT).event(KF_SWITCH_SESSION).handler(this.kfSessionHandler).end();

        // 门店审核事件
        newRouter.rule().async(false).msgType(EVENT).event(POI_CHECK_NOTIFY).handler(this.storeCheckNotifyHandler).end();

        // 自定义菜单事件
        newRouter.rule().async(false).msgType(EVENT).event(EventType.CLICK).handler(this.menuHandler).end();

        // 点击菜单连接事件
        newRouter.rule().async(false).msgType(EVENT).event(EventType.VIEW).handler(this.nullHandler).end();

        // 关注事件
        newRouter.rule().async(false).msgType(EVENT).event(SUBSCRIBE).handler(this.subscribeHandler).end();

        // 取消关注事件
        newRouter.rule().async(false).msgType(EVENT).event(UNSUBSCRIBE).handler(this.unsubscribeHandler).end();

        // 上报地理位置事件
        newRouter.rule().async(false).msgType(EVENT).event(EventType.LOCATION).handler(this.locationHandler).end();

        // 接收地理位置消息
        newRouter.rule().async(false).msgType(XmlMsgType.LOCATION).handler(this.locationHandler).end();

        // 扫码事件
        newRouter.rule().async(false).msgType(EVENT).event(EventType.SCAN).handler(this.scanHandler).end();

        // 默认
        newRouter.rule().async(false).handler(this.msgHandler).end();

        return newRouter;
    }

}
