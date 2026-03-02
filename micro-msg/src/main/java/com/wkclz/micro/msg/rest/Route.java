package com.wkclz.micro.msg.rest;


import com.wkclz.core.annotation.Desc;
import com.wkclz.core.annotation.Router;

/**
 * Description:
 * Created: wangkaicun @ 2017-10-19 上午12:46
 */

@Router(module = "micro-msg", prefix = Route.PREFIX)
public interface Route {

    String PREFIX = "/micro-msg";


    @Desc("01. 管理员消息分页")
    String MANAGER_MSG_PAGE = "/micro-msg/manager/page";
    @Desc("02. 管理员消息发布")
    String MANAGER_MSG_SENT = "/micro-msg/manager/sent";
    @Desc("03. 管理员消息详情")
    String MANAGER_MSG_INFO = "/micro-msg/manager/info";
    @Desc("04. 管理员消息阅读记录")
    String MANAGER_MSG_RECORD_PAGE = "/micro-msg/manager/record/page";

    @Desc("11. 个人消息列表(最多100条,当100时,需要展示为99+)")
    String PERSONAL_MSG_LIST = "/micro-msg/personal/list";
    @Desc("12. 个人消息分页")
    String PERSONAL_MSG_PAGE = "/micro-msg/personal/page";
    @Desc("13. 个人消息详情(阅读)")
    String PERSONAL_MSG_INFO = "/micro-msg/personal/info";
    @Desc("14. 个人消息，批量标注已读")
    String PERSONAL_MSG_READED = "/micro-msg/personal/readed";

    @Desc("21. 个人消息获取配置")
    String PERSONAL_MSG_SETTINGS = "/micro-msg/personal/settings";
    @Desc("22. 个人消息保存配置")
    String PERSONAL_MSG_SETTINGS_SAVE = "/micro-msg/personal/settings/save";

}
