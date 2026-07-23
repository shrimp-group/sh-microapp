package com.wkclz.micro.msg.rest;


import com.wkclz.core.annotation.Router;

/**
 * Description:
 * Created: wangkaicun @ 2017-10-19 上午12:46
 */

@Router(module = "micro-msg", prefix = Route.PREFIX)
public interface Route {

    String PREFIX = "/micro-msg";


    String MANAGER_NOTIFICATION_PAGE = "/manager/notification/page";
    String MANAGER_NOTIFICATION_SENT = "/manager/notification/sent";
    String MANAGER_NOTIFICATION_INFO = "/manager/notification/info";
    String MANAGER_NOTIFICATION_RECORD_PAGE = "/manager/notification/record/page";




    String MANAGER_TEMPLATE_PAGE = "/manager/template/page";
    String MANAGER_TEMPLATE_INFO = "/manager/template/info";
    String MANAGER_TEMPLATE_CREATE = "/manager/template/create";
    String MANAGER_TEMPLATE_UPDATE = "/manager/template/update";
    String MANAGER_TEMPLATE_REMOVE = "/manager/template/remove";






    String PERSONAL_MSG_LIST = "/personal/list";
    String PERSONAL_MSG_PAGE = "/personal/page";
    String PERSONAL_MSG_INFO = "/personal/info";
    String PERSONAL_MSG_READED = "/personal/readed";

    String PERSONAL_MSG_SETTINGS = "/personal/settings";
    String PERSONAL_MSG_SETTINGS_SAVE = "/personal/settings/save";





}
