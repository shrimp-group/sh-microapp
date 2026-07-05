package com.wkclz.micro.dbview.rest;

import com.wkclz.core.annotation.ApiDesc;
import com.wkclz.core.annotation.Router;

@Router(module = "micro-dbview", prefix = Route.PREFIX)
public interface Route {

    String PREFIX = "/micro-dbview";

    @ApiDesc("1. 数据源-分页查询")
    String DATASOURCE_PAGE = "/datasource/page";
    @ApiDesc("2. 数据源-详情")
    String DATASOURCE_INFO = "/datasource/info";
    @ApiDesc("3. 数据源-新增")
    String DATASOURCE_CREATE = "/datasource/create";
    @ApiDesc("4. 数据源-更新")
    String DATASOURCE_UPDATE = "/datasource/update";
    @ApiDesc("5. 数据源-删除")
    String DATASOURCE_REMOVE = "/datasource/remove";
    @ApiDesc("6. 数据源-选项列表")
    String DATASOURCE_OPTIONS = "/datasource/options";
    @ApiDesc("7. 数据源-测试连接")
    String DATASOURCE_TEST_CONNECTION = "/datasource/test-connection";

    @ApiDesc("1. 权限-分页查询")
    String PERMISSION_PAGE = "/permission/page";
    @ApiDesc("2. 权限-新增")
    String PERMISSION_CREATE = "/permission/create";
    @ApiDesc("3. 权限-更新")
    String PERMISSION_UPDATE = "/permission/update";
    @ApiDesc("4. 权限-删除")
    String PERMISSION_REMOVE = "/permission/remove";
    @ApiDesc("5. 权限-当前用户权限列表")
    String PERMISSION_MY = "/permission/my-permissions";

    @ApiDesc("1. 元数据-数据库列表")
    String METADATA_SCHEMAS = "/metadata/schemas";
    @ApiDesc("2. 元数据-表列表")
    String METADATA_TABLES = "/metadata/tables";
    @ApiDesc("3. 元数据-表详情")
    String METADATA_TABLE_DETAIL = "/metadata/table-detail";
    @ApiDesc("4. 元数据-字段列表")
    String METADATA_COLUMNS = "/metadata/columns";
    @ApiDesc("5. 元数据-索引列表")
    String METADATA_INDEXES = "/metadata/indexes";
    @ApiDesc("6. 元数据-建表DDL")
    String METADATA_TABLE_DDL = "/metadata/table-ddl";
    @ApiDesc("7. 元数据-刷新缓存")
    String METADATA_REFRESH_CACHE = "/metadata/refresh-cache";

    @ApiDesc("1. SQL-执行")
    String SQL_EXECUTE = "/sql/execute";
    @ApiDesc("2. SQL-执行历史分页")
    String SQL_HISTORY_PAGE = "/sql/history/page";

    @ApiDesc("1. DDL-添加字段")
    String DDL_ADD_COLUMN = "/ddl/add-column";
    @ApiDesc("2. DDL-删除字段")
    String DDL_DROP_COLUMN = "/ddl/drop-column";
    @ApiDesc("3. DDL-修改字段")
    String DDL_MODIFY_COLUMN = "/ddl/modify-column";
    @ApiDesc("4. DDL-添加索引")
    String DDL_ADD_INDEX = "/ddl/add-index";
    @ApiDesc("5. DDL-删除索引")
    String DDL_DROP_INDEX = "/ddl/drop-index";
    @ApiDesc("6. DDL-重命名表")
    String DDL_RENAME_TABLE = "/ddl/rename-table";
    @ApiDesc("7. DDL-修改表注释")
    String DDL_COMMENT_TABLE = "/ddl/comment-table";
    @ApiDesc("8. DDL-修改字段注释")
    String DDL_COMMENT_COLUMN = "/ddl/comment-column";
    @ApiDesc("9. DDL-预览")
    String DDL_PREVIEW = "/ddl/preview";
    @ApiDesc("10. DDL-执行DDL语句")
    String DDL_EXECUTE_DDL = "/ddl/execute-ddl";
}
