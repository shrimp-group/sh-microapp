package com.wkclz.micro.flowable.rest;

import com.wkclz.core.annotation.Router;

@Router(module = "micro-flowable", prefix = Route.PREFIX)
public interface Route {

    String PREFIX = "/micro-flowable";

    // 管理端 - 流程设计
    String ADMIN_DESIGN_UPLOAD = "/admin/design/upload";
    String ADMIN_DESIGN_PAGE = "/admin/design/page";
    String ADMIN_DESIGN_INFO = "/admin/design/info";
    String ADMIN_DESIGN_UPDATE = "/admin/design/update";
    String ADMIN_DESIGN_REMOVE = "/admin/design/remove";
    String ADMIN_DESIGN_DEPLOY = "/admin/design/deploy";

    // 管理端 - 节点配置
    String ADMIN_NODE_LIST = "/admin/node/list";
    String ADMIN_NODE_INFO = "/admin/node/info";
    String ADMIN_NODE_UPDATE = "/admin/node/update";

    // 管理端 - 透传（流程定义）
    String ADMIN_DEFINITION_PAGE = "/admin/definition/page";
    String ADMIN_DEFINITION_INFO = "/admin/definition/info";
    String ADMIN_DEFINITION_LIST = "/admin/definition/list";

    // 管理端 - 透传（部署记录）
    String ADMIN_DEPLOY_PAGE = "/admin/deploy/page";
    String ADMIN_DEPLOY_REMOVE = "/admin/deploy/remove";

    // 业务端 - 申请
    String APPLY_CREATE = "/apply/create";
    String APPLY_PAGE = "/apply/page";
    String APPLY_INFO = "/apply/info";

    // 业务端 - 任务
    String TASK_TODO_PAGE = "/task/todo/page";
    String TASK_DONE_PAGE = "/task/done/page";
    String TASK_INFO = "/task/info";
    String TASK_COMPLETE = "/task/complete";
    String TASK_CLAIM = "/task/claim";
    String TASK_UNCLAIM = "/task/unclaim";
    String TASK_REJECT = "/task/reject";
    String TASK_TRANSFER = "/task/transfer";
    String TASK_DELEGATE = "/task/delegate";

    // 业务端 - 实例/历史
    String INSTANCE_PAGE = "/instance/page";
    String INSTANCE_INFO = "/instance/info";
    String INSTANCE_WITHDRAW = "/instance/withdraw";
    String HISTORY_INSTANCE_PAGE = "/history/instance/page";
    String HISTORY_TASK_PAGE = "/history/task/page";
    String HISTORY_ACTIVITY_LIST = "/history/activity/list";

    // 业务端 - 审批意见
    String APPROVAL_LIST = "/approval/list";

    // 异常监控
    String ERROR_PAGE = "/error/page";
    String ERROR_INFO = "/error/info";
    String ERROR_HANDLE = "/error/handle";
}
