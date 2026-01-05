package com.wkclz.micro.wxapp.config;

import cn.binarywang.wx.miniapp.api.WxMaService;
import cn.binarywang.wx.miniapp.api.impl.WxMaServiceImpl;
import cn.binarywang.wx.miniapp.bean.WxMaKefuMessage;
import cn.binarywang.wx.miniapp.config.impl.WxMaDefaultConfigImpl;
import cn.binarywang.wx.miniapp.message.WxMaMessageHandler;
import cn.binarywang.wx.miniapp.message.WxMaMessageRouter;
import com.google.common.collect.Maps;
import com.wkclz.core.exception.ValidationException;
import com.wkclz.iam.sdk.helper.SessionHelper;
import com.wkclz.micro.wxapp.bean.vo.WxMaAppInfo;
import com.wkclz.micro.wxapp.service.WxappConfigService;
import me.chanjar.weixin.common.bean.result.WxMediaUploadResult;
import me.chanjar.weixin.common.error.WxErrorException;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.Assert;

import java.io.File;
import java.util.Map;

/**
 * @author <a href="https://github.com/binarywang">Binary Wang</a>
 */
@Configuration
public class WxMaConfiguration {

    private static final Map<String, WxMaService> MA_TENANT_SERVICES = Maps.newHashMap();
    private static final Map<String, WxMaMessageRouter> ROUTERS = Maps.newHashMap();

    private static final Map<String, String> MA_APPID_TENANT = Maps.newHashMap();

    @Autowired
    private WxappConfigService wxappConfigService;

    public WxMaService getMaService(String appid) {
        Assert.notNull(appid, "appid 不能为空");
        String tenantCode = MA_APPID_TENANT.get(appid);
        if (tenantCode == null) {
            init(appid);
            tenantCode =  MA_APPID_TENANT.get(appid);
        }
        return MA_TENANT_SERVICES.get(tenantCode);
    }

    public WxMaService getMaService() {
        String tenantCode = SessionHelper.getTenantCode();
        if (StringUtils.isBlank(tenantCode)) {
            throw new IllegalArgumentException("找不到域名信息，请核实！");
        }
        WxMaService wxService = MA_TENANT_SERVICES.get(tenantCode);
        if (wxService == null) {
            init(null);
            wxService = MA_TENANT_SERVICES.get(tenantCode);
        }
        return wxService;
    }

    public WxMaMessageRouter getRouter(String appid) {
        Assert.notNull(appid, "appid 不能为空");
        String tenantCode = MA_APPID_TENANT.get(appid);
        if (StringUtils.isBlank(tenantCode)) {
            init(appid);
            tenantCode =  MA_APPID_TENANT.get(appid);
        }
        return ROUTERS.get(tenantCode);
    }

    public WxMaMessageRouter getRouter() {
        String tenantCode = SessionHelper.getTenantCode();
        if (StringUtils.isBlank(tenantCode)) {
            throw new IllegalArgumentException("找不到域名信息，请核实！");
        }
        WxMaMessageRouter router = ROUTERS.get(tenantCode);
        if (router == null) {
            init(null);
            router = ROUTERS.get(tenantCode);
        }
        return router;
    }


    private synchronized void init(String appid) {

        WxMaAppInfo wxApp = new WxMaAppInfo();
        wxApp.setAppId(appid);
        String tenantCode = SessionHelper.getTenantCode();
        if (StringUtils.isNotBlank(tenantCode)) {
            wxApp.setTenantCode(tenantCode);
        }

        wxApp = wxappConfigService.getWxMaAppInfo(wxApp);
        if (wxApp == null) {
            throw ValidationException.of("没有找到小程序配置：appid: {}, tenantCode: {}", appid, tenantCode);
        }
        WxMaDefaultConfigImpl config = new WxMaDefaultConfigImpl();
        config.setAppid(wxApp.getAppId());
        config.setSecret(wxApp.getAppSecret());
        config.setToken(wxApp.getAppToken());
        config.setAesKey(wxApp.getAesKey());
        // default jdson
        config.setMsgDataFormat("JSON");

        WxMaService service = new WxMaServiceImpl();
        service.setWxMaConfig(config);
        ROUTERS.put(tenantCode, this.newRouter(service));
        MA_TENANT_SERVICES.put(tenantCode, service);
        MA_APPID_TENANT.put(wxApp.getAppId(), wxApp.getTenantCode());
    }




    private WxMaMessageRouter newRouter(WxMaService service) {
        final WxMaMessageRouter router = new WxMaMessageRouter(service);
        router
            .rule().handler(logHandler).next()
            .rule().async(false).content("模板").handler(templateMsgHandler).end()
            .rule().async(false).content("文本").handler(textHandler).end()
            .rule().async(false).content("图片").handler(picHandler).end()
            .rule().async(false).content("二维码").handler(qrcodeHandler).end();
        return router;
    }

    private final WxMaMessageHandler templateMsgHandler = (wxMessage, context, service, sessionManager) -> {
        /* TODO 新版本不再支持，需要找解决方案
        service.getMsgService().sendTemplateMsg(WxMaTemplateMessage.builder()
            .templateId("此处更换为自己的模板id")
            .formId("自己替换可用的formid")
            .data(Lists.newArrayList(
                new WxMaTemplateData("keyword1", "339208499", "#173177")))
            .toUser(wxMessage.getFromUser())
            .build());
        */
        return null;
    };

    private final WxMaMessageHandler logHandler = (wxMessage, context, service, sessionManager) -> {
        System.out.println("收到消息：" + wxMessage.toString());
        service.getMsgService().sendKefuMsg(WxMaKefuMessage.newTextBuilder().content("收到信息为：" + wxMessage.toJson())
            .toUser(wxMessage.getFromUser()).build());
        return null;
    };

    private final WxMaMessageHandler textHandler = (wxMessage, context, service, sessionManager) -> {
        service.getMsgService().sendKefuMsg(WxMaKefuMessage.newTextBuilder().content("回复文本消息")
            .toUser(wxMessage.getFromUser()).build());
        return null;
    };

    private final WxMaMessageHandler picHandler = (wxMessage, context, service, sessionManager) -> {
        try {
            WxMediaUploadResult uploadResult = service.getMediaService()
                .uploadMedia("image", "png",
                    ClassLoader.getSystemResourceAsStream("tmp.png"));
            service.getMsgService().sendKefuMsg(
                WxMaKefuMessage
                    .newImageBuilder()
                    .mediaId(uploadResult.getMediaId())
                    .toUser(wxMessage.getFromUser())
                    .build());
        } catch (WxErrorException e) {
            e.printStackTrace();
        }

        return null;
    };

    private final WxMaMessageHandler qrcodeHandler = (wxMessage, context, service, sessionManager) -> {
        try {
            final File file = service.getQrcodeService().createQrcode("123", 430);
            WxMediaUploadResult uploadResult = service.getMediaService().uploadMedia("image", file);
            service.getMsgService().sendKefuMsg(
                WxMaKefuMessage
                    .newImageBuilder()
                    .mediaId(uploadResult.getMediaId())
                    .toUser(wxMessage.getFromUser())
                    .build());
        } catch (WxErrorException e) {
            e.printStackTrace();
        }

        return null;
    };

}
