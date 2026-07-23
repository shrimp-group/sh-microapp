package com.wkclz.micro.dbview.rest;

import com.wkclz.core.annotation.Router;

@Router(module = "micro-dbview", prefix = Route.PREFIX)
public interface Route {

    String PREFIX = "/micro-dbview";

    String DATASOURCE_PAGE = "/datasource/page";
    String DATASOURCE_INFO = "/datasource/info";
    String DATASOURCE_CREATE = "/datasource/create";
    String DATASOURCE_UPDATE = "/datasource/update";
    String DATASOURCE_REMOVE = "/datasource/remove";
    String DATASOURCE_OPTIONS = "/datasource/options";
    String DATASOURCE_TEST_CONNECTION = "/datasource/test-connection";

    String PERMISSION_PAGE = "/permission/page";
    String PERMISSION_CREATE = "/permission/create";
    String PERMISSION_UPDATE = "/permission/update";
    String PERMISSION_REMOVE = "/permission/remove";
    String PERMISSION_MY = "/permission/my-permissions";

    String METADATA_SCHEMAS = "/metadata/schemas";
    String METADATA_TABLES = "/metadata/tables";
    String METADATA_TABLE_DETAIL = "/metadata/table-detail";
    String METADATA_COLUMNS = "/metadata/columns";
    String METADATA_INDEXES = "/metadata/indexes";
    String METADATA_TABLE_DDL = "/metadata/table-ddl";
    String METADATA_REFRESH_CACHE = "/metadata/refresh-cache";

    String SQL_EXECUTE = "/sql/execute";
    String SQL_HISTORY_PAGE = "/sql/history/page";

    String DDL_ADD_COLUMN = "/ddl/add-column";
    String DDL_DROP_COLUMN = "/ddl/drop-column";
    String DDL_MODIFY_COLUMN = "/ddl/modify-column";
    String DDL_ADD_INDEX = "/ddl/add-index";
    String DDL_DROP_INDEX = "/ddl/drop-index";
    String DDL_RENAME_TABLE = "/ddl/rename-table";
    String DDL_COMMENT_TABLE = "/ddl/comment-table";
    String DDL_COMMENT_COLUMN = "/ddl/comment-column";
    String DDL_PREVIEW = "/ddl/preview";
    String DDL_EXECUTE_DDL = "/ddl/execute-ddl";
}
