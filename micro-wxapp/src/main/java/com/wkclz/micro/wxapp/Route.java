package com.wkclz.micro.wxapp;

import com.wkclz.core.annotation.ApiDesc;
import com.wkclz.core.annotation.Router;

@Router(module = "micro-wxapp", prefix = Route.PREFIX)
public interface Route {

    String PREFIX = "/micro-wxapp";




    @ApiDesc("1. 微信小程序-配置-分页")
    String WXAPP_CONFIG_PAGE = "/config/page";
    @ApiDesc("2. 微信小程序-配置-详情")
    String WXAPP_CONFIG_INFO = "/config/info";
    @ApiDesc("3. 微信小程序-配置-创建")
    String WXAPP_CONFIG_CREATE = "/config/create";
    @ApiDesc("4. 微信小程序-配置-更新")
    String WXAPP_CONFIG_UPDATE = "/config/update";
    @ApiDesc("5. 微信小程序-配置-删除")
    String WXAPP_CONFIG_REMOVE = "/config/remove";



    @ApiDesc("1. 小程序登录")
    String MINIAPP_LOGIN = "/public/miniapp/login";
    @ApiDesc("2. 小程序用户信息")
    String MINIAPP_USERINFO = "/miniapp/userinfo";
    @ApiDesc("3. 小程序更新用户信息")
    String MINIAPP_USERINFO_UPDATE = "/miniapp/userinfo/update";
    @ApiDesc("4. 小程序绑定手机")
    String MINIAPP_MOBILE_BIND = "/miniapp/mobile/bind";


    @ApiDesc("7. personal-绑定微信【未完成】")
    String PERSONAL_WECHAT_BIND = "/personal/wechat/bind";
    @ApiDesc("8. personal-解除绑定微信【未完成】")
    String PERSONAL_WECHAT_UNBIND = "/personal/wechat/unbind";

    @ApiDesc("8.从小程序内获取手机号")
    String CUSTOMER_WX_USER_PHONE = "/customer/wx_user/phone";
    @ApiDesc("1. miniapp-微信验签[暂时废弃]")
    String CUSTOMER_WX_PORTAL = "/public/miniapp/portal";



    @ApiDesc("6. customer-上传临时素材")
    String CUSTOMER_WX_MEDIA_UPLOAD = "/customer/wx/media/upload";
    @ApiDesc("7. customer-下载临时素材")
    String CUSTOMER_WX_MEDIA_DOWNLOAD = "/customer/wx/media/download/{mediaId}";



}
