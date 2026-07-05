package com.wkclz.micro.material.rest;

import com.wkclz.core.annotation.ApiDesc;
import com.wkclz.core.annotation.Router;

@Router(module = "micro-material", prefix = Route.PREFIX)
public interface Route {

    String PREFIX = "/micro-material";

    // ==================== manage 分组（管理类接口） ====================

    @ApiDesc("1. 素材-分页查询")
    String MATERIAL_PAGE = "/manage/material/page";
    @ApiDesc("2. 素材-详情")
    String MATERIAL_INFO = "/manage/material/info";
    @ApiDesc("3. 素材-创建")
    String MATERIAL_CREATE = "/manage/material/create";
    @ApiDesc("4. 素材-批量创建")
    String MATERIAL_BATCH_CREATE = "/manage/material/batch-create";
    @ApiDesc("5. 素材-链接引入")
    String MATERIAL_LINK_CREATE = "/manage/material/link-create";
    @ApiDesc("6. 素材-修改")
    String MATERIAL_UPDATE = "/manage/material/update";
    @ApiDesc("7. 素材-删除")
    String MATERIAL_REMOVE = "/manage/material/remove";
    @ApiDesc("8. 素材-恢复")
    String MATERIAL_RESTORE = "/manage/material/restore";
    @ApiDesc("9. 素材-移动分组")
    String MATERIAL_MOVE = "/manage/material/move";
    @ApiDesc("10. 素材-替换文件")
    String MATERIAL_REPLACE_FILE = "/manage/material/replace-file";
    @ApiDesc("11. 素材-修改可见性")
    String MATERIAL_VISIBILITY = "/manage/material/visibility";
    @ApiDesc("12. 素材-链接有效性检测")
    String MATERIAL_LINK_CHECK = "/manage/material/link-check";

    @ApiDesc("13. 分组-树")
    String GROUP_TREE = "/manage/group/tree";
    @ApiDesc("14. 分组-详情")
    String GROUP_INFO = "/manage/group/info";
    @ApiDesc("15. 分组-新增")
    String GROUP_CREATE = "/manage/group/create";
    @ApiDesc("16. 分组-修改")
    String GROUP_UPDATE = "/manage/group/update";
    @ApiDesc("17. 分组-删除")
    String GROUP_REMOVE = "/manage/group/remove";
    @ApiDesc("18. 分组-移动")
    String GROUP_MOVE = "/manage/group/move";
    @ApiDesc("19. 分组-排序")
    String GROUP_SORT = "/manage/group/sort";

    @ApiDesc("20. 引用-注册")
    String REF_BIND = "/manage/ref/bind";
    @ApiDesc("21. 引用-解绑")
    String REF_UNBIND = "/manage/ref/unbind";
    @ApiDesc("22. 引用-列表")
    String REF_LIST = "/manage/ref/list";
    @ApiDesc("23. 引用-检测")
    String REF_CHECK = "/manage/ref/check";

    @ApiDesc("24. 版本-列表")
    String VERSION_LIST = "/manage/version/list";
    @ApiDesc("25. 版本-回滚")
    String VERSION_ROLLBACK = "/manage/version/rollback";

    @ApiDesc("26. 转移-执行")
    String TRANSFER_CREATE = "/manage/transfer/create";
    @ApiDesc("27. 转移-记录")
    String TRANSFER_LOG = "/manage/transfer/log";

    @ApiDesc("28. 统计-热门")
    String STATS_HOT = "/manage/stats/hot";
    @ApiDesc("29. 统计-分布")
    String STATS_DISTRIBUTION = "/manage/stats/distribution";

    // ==================== customer 分组（客户类接口） ====================

    @ApiDesc("30. 选择器-素材列表")
    String PICKER_LIST = "/customer/picker/list";
    @ApiDesc("31. 选择器-分组树")
    String PICKER_GROUPS = "/customer/picker/groups";
}
