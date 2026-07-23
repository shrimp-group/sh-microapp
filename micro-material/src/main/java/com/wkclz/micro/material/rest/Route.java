package com.wkclz.micro.material.rest;

import com.wkclz.core.annotation.Router;

@Router(module = "micro-material", prefix = Route.PREFIX)
public interface Route {

    String PREFIX = "/micro-material";

    // ==================== manage 分组（管理类接口） ====================

    String MATERIAL_PAGE = "/manage/material/page";
    String MATERIAL_INFO = "/manage/material/info";
    String MATERIAL_CREATE = "/manage/material/create";
    String MATERIAL_BATCH_CREATE = "/manage/material/batch-create";
    String MATERIAL_LINK_CREATE = "/manage/material/link-create";
    String MATERIAL_UPDATE = "/manage/material/update";
    String MATERIAL_REMOVE = "/manage/material/remove";
    String MATERIAL_RESTORE = "/manage/material/restore";
    String MATERIAL_MOVE = "/manage/material/move";
    String MATERIAL_REPLACE_FILE = "/manage/material/replace-file";
    String MATERIAL_VISIBILITY = "/manage/material/visibility";
    String MATERIAL_LINK_CHECK = "/manage/material/link-check";

    String GROUP_TREE = "/manage/group/tree";
    String GROUP_INFO = "/manage/group/info";
    String GROUP_CREATE = "/manage/group/create";
    String GROUP_UPDATE = "/manage/group/update";
    String GROUP_REMOVE = "/manage/group/remove";
    String GROUP_MOVE = "/manage/group/move";
    String GROUP_SORT = "/manage/group/sort";

    String REF_BIND = "/manage/ref/bind";
    String REF_UNBIND = "/manage/ref/unbind";
    String REF_LIST = "/manage/ref/list";
    String REF_CHECK = "/manage/ref/check";

    String VERSION_LIST = "/manage/version/list";
    String VERSION_ROLLBACK = "/manage/version/rollback";

    String TRANSFER_CREATE = "/manage/transfer/create";
    String TRANSFER_LOG = "/manage/transfer/log";

    String STATS_HOT = "/manage/stats/hot";
    String STATS_DISTRIBUTION = "/manage/stats/distribution";

    // ==================== customer 分组（客户类接口） ====================

    String PICKER_LIST = "/customer/picker/list";
    String PICKER_GROUPS = "/customer/picker/groups";
}
