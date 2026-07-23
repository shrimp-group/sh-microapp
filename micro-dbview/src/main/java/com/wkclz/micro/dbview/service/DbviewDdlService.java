package com.wkclz.micro.dbview.service;

import com.wkclz.core.exception.ValidationException;
import com.wkclz.core.identity.IdentityContext;
import com.wkclz.dynamicdb.DynamicDataSourceHolder;
import com.wkclz.micro.dbview.bean.dto.*;
import com.wkclz.micro.dbview.bean.entity.DbviewDatasource;
import com.wkclz.micro.dbview.bean.enums.SqlType;
import com.wkclz.micro.dbview.config.DbviewConfig;
import com.wkclz.micro.dbview.mapper.DbviewDatasourceMapper;
import com.wkclz.mybatis.bean.DataSourceInfo;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Connection;
import java.sql.Statement;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Service
public class DbviewDdlService {

    @Autowired
    private DbviewDatasourceMapper datasourceMapper;

    @Autowired
    private DbviewConnectionService connectionService;

    @Autowired
    private DbviewDatasourcePermissionService permissionService;

    @Autowired
    private DbviewSqlHistoryService sqlHistoryService;

    @Autowired
    private DbviewMetadataService metadataService;

    @Autowired
    private DbviewConfig dbviewConfig;

    public DdlPreview preview(DdlRequest request) {
        String ddl = generateDdl(request);
        DdlPreview preview = new DdlPreview();
        preview.setDdl(ddl);
        preview.setDatasourceId(request.getDatasourceId());
        preview.setSchemaName(request.getSchemaName());
        preview.setTableName(request.getTableName());
        return preview;
    }

    public void execute(DdlRequest request) {
        String userCode = IdentityContext.getUserCode();
        permissionService.check(request.getDatasourceId(), "ALTER TABLE", true);

        String ddl = generateDdl(request);
        executeDdlInternal(request.getDatasourceId(), ddl, userCode);
    }

    public void executeDdl(Long datasourceId, String schemaName, String ddl, Boolean confirmDangerous) {
        String userCode = IdentityContext.getUserCode();
        permissionService.check(datasourceId, ddl, confirmDangerous);
        executeDdlInternal(datasourceId, ddl, userCode);
    }

    private void executeDdlInternal(Long datasourceId, String ddl, String userCode) {
        DbviewDatasource ds = datasourceMapper.selectById(datasourceId);
        if (ds == null) {
            throw ValidationException.of("数据源不存在: " + datasourceId);
        }
        String password = connectionService.decryptPassword(ds.getPassword());
        DataSourceInfo info = new DataSourceInfo();
        info.setUrl(ds.getJdbcUrl());
        info.setUsername(ds.getUsername());
        info.setPassword(password);

        long startTime = System.currentTimeMillis();
        try {
            CompletableFuture.runAsync(() -> {
                DynamicDataSourceHolder.set(String.valueOf(datasourceId));
                try (Connection conn = DataSourceInfo.getConnect(info);
                     Statement stmt = conn.createStatement()) {
                    stmt.execute(ddl);
                } catch (Exception e) {
                    throw new RuntimeException(e.getMessage(), e);
                } finally {
                    DynamicDataSourceHolder.clear();
                }
            }).join();

            long costMs = System.currentTimeMillis() - startTime;
            sqlHistoryService.insertHistory(datasourceId, userCode, ddl,
                    SqlType.ALTER.name(), 1, 0L, costMs, null);
            metadataService.refreshCache(datasourceId);
        } catch (Exception e) {
            long costMs = System.currentTimeMillis() - startTime;
            String errorMsg = e.getCause() != null ? e.getCause().getMessage() : e.getMessage();
            sqlHistoryService.insertHistory(datasourceId, userCode, ddl,
                    SqlType.ALTER.name(), 0, 0L, costMs, errorMsg);
            throw ValidationException.of("DDL执行失败: " + errorMsg);
        }
    }

    private String generateDdl(DdlRequest request) {
        String qualifiedTable = getQualifiedTable(request.getSchemaName(), request.getTableName());

        return switch (request.getDdlType()) {
            case ADD_COLUMN -> generateAddColumn(qualifiedTable, request.getColumn());
            case DROP_COLUMN -> generateDropColumn(qualifiedTable, request.getColumn());
            case MODIFY_COLUMN -> generateModifyColumn(qualifiedTable, request.getColumn());
            case ADD_INDEX -> generateAddIndex(qualifiedTable, request.getIndex());
            case DROP_INDEX -> generateDropIndex(qualifiedTable, request.getIndex());
            case RENAME_TABLE -> generateRenameTable(qualifiedTable, request.getNewTableName(), request.getSchemaName());
            case COMMENT_TABLE -> generateCommentTable(qualifiedTable, request.getTableComment());
            case COMMENT_COLUMN -> generateCommentColumn(qualifiedTable, request.getDatasourceId(), request.getSchemaName(), request.getTableName(), request.getColumn());
        };
    }

    private String generateAddColumn(String qualifiedTable, ColumnDefinition column) {
        if (column == null || StringUtils.isBlank(column.getColumnName())) {
            throw ValidationException.of("字段定义不能为空");
        }
        StringBuilder sb = new StringBuilder();
        sb.append("ALTER TABLE ").append(qualifiedTable);
        sb.append(" ADD COLUMN `").append(column.getColumnName()).append("` ");
        sb.append(column.getColumnType());
        if (Boolean.FALSE.equals(column.getNullable())) {
            sb.append(" NOT NULL");
        }
        if (column.getDefaultValue() != null) {
            sb.append(" DEFAULT '").append(escapeSql(column.getDefaultValue())).append("'");
        }
        if (StringUtils.isNotBlank(column.getColumnComment())) {
            sb.append(" COMMENT '").append(escapeSql(column.getColumnComment())).append("'");
        }
        if (StringUtils.isNotBlank(column.getAfterColumn())) {
            sb.append(" AFTER `").append(column.getAfterColumn()).append("`");
        }
        return sb.toString();
    }

    private String generateDropColumn(String qualifiedTable, ColumnDefinition column) {
        if (column == null || StringUtils.isBlank(column.getColumnName())) {
            throw ValidationException.of("字段名不能为空");
        }
        return "ALTER TABLE " + qualifiedTable + " DROP COLUMN `" + column.getColumnName() + "`";
    }

    private String generateModifyColumn(String qualifiedTable, ColumnDefinition column) {
        if (column == null || StringUtils.isBlank(column.getColumnName())) {
            throw ValidationException.of("字段定义不能为空");
        }
        if (StringUtils.isBlank(column.getColumnType())) {
            throw ValidationException.of("修改字段时必须指定字段类型");
        }
        StringBuilder sb = new StringBuilder();
        sb.append("ALTER TABLE ").append(qualifiedTable);
        sb.append(" MODIFY COLUMN `").append(column.getColumnName()).append("` ");
        sb.append(column.getColumnType());
        if (Boolean.FALSE.equals(column.getNullable())) {
            sb.append(" NOT NULL");
        }
        if (column.getDefaultValue() != null) {
            sb.append(" DEFAULT '").append(escapeSql(column.getDefaultValue())).append("'");
        }
        if (StringUtils.isNotBlank(column.getColumnComment())) {
            sb.append(" COMMENT '").append(escapeSql(column.getColumnComment())).append("'");
        }
        if (StringUtils.isNotBlank(column.getAfterColumn())) {
            sb.append(" AFTER `").append(column.getAfterColumn()).append("`");
        }
        return sb.toString();
    }

    private String generateAddIndex(String qualifiedTable, IndexDefinition index) {
        if (index == null || StringUtils.isBlank(index.getIndexName())) {
            throw ValidationException.of("索引定义不能为空");
        }
        if (index.getColumnNames() == null || index.getColumnNames().isEmpty()) {
            throw ValidationException.of("索引字段不能为空");
        }
        StringBuilder sb = new StringBuilder();
        sb.append("ALTER TABLE ").append(qualifiedTable);
        sb.append(" ADD ");
        if (Boolean.TRUE.equals(index.getUnique())) {
            sb.append("UNIQUE ");
        }
        sb.append("INDEX `").append(index.getIndexName()).append("` (");
        String columns = index.getColumnNames().stream()
                .map(c -> "`" + c + "`")
                .collect(Collectors.joining(", "));
        sb.append(columns).append(")");
        if (StringUtils.isNotBlank(index.getComment())) {
            sb.append(" COMMENT '").append(escapeSql(index.getComment())).append("'");
        }
        return sb.toString();
    }

    private String generateDropIndex(String qualifiedTable, IndexDefinition index) {
        if (index == null || StringUtils.isBlank(index.getIndexName())) {
            throw ValidationException.of("索引名不能为空");
        }
        return "ALTER TABLE " + qualifiedTable + " DROP INDEX `" + index.getIndexName() + "`";
    }

    private String generateRenameTable(String qualifiedTable, String newTableName, String schemaName) {
        if (StringUtils.isBlank(newTableName)) {
            throw ValidationException.of("新表名不能为空");
        }
        String newQualifiedTable = getQualifiedTable(schemaName, newTableName);
        return "RENAME TABLE " + qualifiedTable + " TO " + newQualifiedTable;
    }

    private String generateCommentTable(String qualifiedTable, String tableComment) {
        if (StringUtils.isBlank(tableComment)) {
            throw ValidationException.of("表注释不能为空");
        }
        return "ALTER TABLE " + qualifiedTable + " COMMENT '" + escapeSql(tableComment) + "'";
    }

    private String generateCommentColumn(String qualifiedTable, Long datasourceId, String schemaName, String tableName, ColumnDefinition column) {
        if (column == null || StringUtils.isBlank(column.getColumnName())) {
            throw ValidationException.of("字段名不能为空");
        }
        if (StringUtils.isBlank(column.getColumnComment())) {
            throw ValidationException.of("字段注释不能为空");
        }

        String columnType = column.getColumnType();
        Boolean nullable = column.getNullable();
        if (StringUtils.isBlank(columnType) || nullable == null) {
            ColumnInfo currentColumn = findCurrentColumn(datasourceId, schemaName, tableName, column.getColumnName());
            if (currentColumn != null) {
                if (StringUtils.isBlank(columnType)) {
                    columnType = currentColumn.getColumnType();
                }
                if (nullable == null) {
                    nullable = currentColumn.getNullable();
                }
            }
        }
        if (StringUtils.isBlank(columnType)) {
            throw ValidationException.of("无法获取字段 " + column.getColumnName() + " 的当前类型，请手动指定 columnType");
        }

        StringBuilder sb = new StringBuilder();
        sb.append("ALTER TABLE ").append(qualifiedTable);
        sb.append(" MODIFY COLUMN `").append(column.getColumnName()).append("` ");
        sb.append(columnType);
        if (Boolean.FALSE.equals(nullable)) {
            sb.append(" NOT NULL");
        }
        sb.append(" COMMENT '").append(escapeSql(column.getColumnComment())).append("'");
        return sb.toString();
    }

    private ColumnInfo findCurrentColumn(Long datasourceId, String schemaName, String tableName, String columnName) {
        try {
            List<ColumnInfo> columns = metadataService.getColumns(datasourceId, schemaName, tableName);
            if (columns != null) {
                for (ColumnInfo col : columns) {
                    if (columnName.equals(col.getColumnName())) {
                        return col;
                    }
                }
            }
        } catch (Exception e) {
            return null;
        }
        return null;
    }

    private String getQualifiedTable(String schemaName, String tableName) {
        if (StringUtils.isNotBlank(schemaName)) {
            return "`" + schemaName + "`.`" + tableName + "`";
        }
        return "`" + tableName + "`";
    }

    private String escapeSql(String value) {
        if (value == null) {
            return null;
        }
        return value.replace("'", "\\'");
    }
}
