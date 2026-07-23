package com.wkclz.micro.wxapp;

import com.wkclz.core.annotation.Router;

@Router(module = "micro-wxapp", prefix = Route.PREFIX)
public interface Route {

    String PREFIX = "/micro-wxapp";




    String WXAPP_CONFIG_PAGE = "/config/page";
    String WXAPP_CONFIG_INFO = "/config/info";
    String WXAPP_CONFIG_CREATE = "/config/create";
    String WXAPP_CONFIG_UPDATE = "/config/update";
    String WXAPP_CONFIG_REMOVE = "/config/remove";

    String WXAPP_USER_PAGE = "/user/page";



    String MINIAPP_LOGIN = "/public/miniapp/login";
    String MINIAPP_USERINFO = "/miniapp/userinfo";
    String MINIAPP_USERINFO_UPDATE = "/miniapp/userinfo/update";
    String MINIAPP_MOBILE_BIND = "/miniapp/mobile/bind";


    String PERSONAL_WECHAT_BIND = "/personal/wechat/bind";
    String PERSONAL_WECHAT_UNBIND = "/personal/wechat/unbind";

    String CUSTOMER_WX_USER_PHONE = "/customer/wx_user/phone";
    String CUSTOMER_WX_PORTAL = "/public/miniapp/portal";



    String CUSTOMER_WX_MEDIA_UPLOAD = "/customer/wx/media/upload";
    String CUSTOMER_WX_MEDIA_DOWNLOAD = "/customer/wx/media/download/{mediaId}";



}
