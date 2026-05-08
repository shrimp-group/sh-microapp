package com.wkclz.micro.msg.rest;


import com.wkclz.core.annotation.ApiDesc;
import com.wkclz.core.annotation.Router;

/**
 * Description:
 * Created: wangkaicun @ 2017-10-19 上午12:46
 */

@Router(module = "micro-msg", prefix = Route.PREFIX)
public interface Route {

    String PREFIX = "/micro-msg";


    @ApiDesc("01. 管理员消息分页")
    String MANAGER_NOTIFICATION_PAGE = "/manager/notification/page";
    @ApiDesc("02. 管理员消息发布")
    String MANAGER_NOTIFICATION_SENT = "/manager/notification/sent";
    @ApiDesc("03. 管理员消息详情")
    String MANAGER_NOTIFICATION_INFO = "/manager/notification/info";
    @ApiDesc("04. 管理员消息阅读记录")
    String MANAGER_NOTIFICATION_RECORD_PAGE = "/manager/notification/record/page";




    @ApiDesc("1. 消息模板-分页")
    String MANAGER_TEMPLATE_PAGE = "/manager/template/page";
    @ApiDesc("2. 消息模板-详情")
    String MANAGER_TEMPLATE_INFO = "/manager/template/info";
    @ApiDesc("3. 消息模板-创建")
    String MANAGER_TEMPLATE_CREATE = "/manager/template/create";
    @ApiDesc("4. 消息模板-修改")
    String MANAGER_TEMPLATE_UPDATE = "/manager/template/update";
    @ApiDesc("5. 消息模板-删除")
    String MANAGER_TEMPLATE_REMOVE = "/manager/template/remove";






    @ApiDesc("11. 个人消息列表(最多100条,当100时,需要展示为99+)")
    String PERSONAL_MSG_LIST = "/personal/list";
    @ApiDesc("12. 个人消息分页")
    String PERSONAL_MSG_PAGE = "/personal/page";
    @ApiDesc("13. 个人消息详情(阅读)")
    String PERSONAL_MSG_INFO = "/personal/info";
    @ApiDesc("14. 个人消息，批量标注已读")
    String PERSONAL_MSG_READED = "/personal/readed";

    @ApiDesc("21. 个人消息获取配置")
    String PERSONAL_MSG_SETTINGS = "/personal/settings";
    @ApiDesc("22. 个人消息保存配置")
    String PERSONAL_MSG_SETTINGS_SAVE = "/personal/settings/save";





}
