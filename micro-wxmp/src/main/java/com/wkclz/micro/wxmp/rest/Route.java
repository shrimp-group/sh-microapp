package com.wkclz.micro.wxmp.rest;


import com.wkclz.core.annotation.Desc;
import com.wkclz.core.annotation.Router;

/**
 * Description:
 * Created: wangkaicun @ 2017-10-19 上午12:46
 */


@Router(module = "micro-wxmp", prefix = Route.PREFIX)
public interface Route {

    String PREFIX = "/micro-wxmp";


    /**
     * 管理端相关接口
     */

    @Desc("1. 微信公众号-配置-分页")
    String WXMP_CONFIG_PAGE = "/config/page";
    @Desc("2. 微信公众号-配置-详情")
    String WXMP_CONFIG_INFO = "/config/info";
    @Desc("3. 微信公众号-配置-创建")
    String WXMP_CONFIG_CREATE = "/config/create";
    @Desc("4. 微信公众号-配置-更新")
    String WXMP_CONFIG_UPDATE = "/config/update";
    @Desc("5. 微信公众号-配置-删除")
    String WXMP_CONFIG_REMOVE = "/config/remove";


    @Desc("1. 客服消息-分页")
    String WXMP_KF_MSG_PAGE = "/kf/msg/page";
    @Desc("2. 客服消息-详情")
    String WXMP_KF_MSG_INFO = "/kf/msg/info";





    @Desc("微信公众号-获取永久素材")
    String WXMP_MATERIAL_BATCHGET_MATERIAL = "/material/batchget_material/{appid}";

    @Desc("微信公众号-菜单-设置")
    String WXMP_MENU_UPDATE = "/menu/update/{appid}";
    @Desc("微信公众号-菜单-删除")
    String WXMP_MENU_DELETE = "/menu/delete/{appid}";



    /**
     * 公众号相关接口
     */

    @Desc("微信公众号-验签")
    String PUBLIC_WXMP_PORTAL_APPID = "/public/portal/{appid}";
    @Desc("微信公众号请求登录")
    String PUBLIC_WXMP_LOGIN_APPID = "/public/login/{appid}";

    /**
     * 客户端相关接口
     */



    /**
     * 我的
     */
    @Desc("1. H5-用户-基本信息")
    String H5_MINE_USERINFO = "/h5/mine/userinfo";



    @Desc("2. H5-微信签名")
    String H5_WX_SIGN = "/h5/wx/sign";


}
