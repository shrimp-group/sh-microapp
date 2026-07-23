package com.wkclz.micro.wxmp.rest;


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

    String WXMP_CONFIG_PAGE = "/config/page";
    String WXMP_CONFIG_INFO = "/config/info";
    String WXMP_CONFIG_CREATE = "/config/create";
    String WXMP_CONFIG_UPDATE = "/config/update";
    String WXMP_CONFIG_REMOVE = "/config/remove";


    String WXMP_KF_MSG_PAGE = "/kf/msg/page";
    String WXMP_KF_MSG_INFO = "/kf/msg/info";





    String WXMP_MATERIAL_BATCHGET_MATERIAL = "/material/batchget_material/{appid}";

    String WXMP_MENU_UPDATE = "/menu/update/{appid}";
    String WXMP_MENU_DELETE = "/menu/delete/{appid}";



    /**
     * 公众号相关接口
     */

    String PUBLIC_WXMP_PORTAL_APPID = "/public/portal/{appid}";
    String PUBLIC_WXMP_LOGIN_APPID = "/public/login/{appid}";

    /**
     * 客户端相关接口
     */



    /**
     * 我的
     */
    String H5_MINE_USERINFO = "/h5/mine/userinfo";



    String H5_WX_SIGN = "/h5/wx/sign";


}
