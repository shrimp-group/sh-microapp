package com.wkclz.micro.dbview.service;

import com.wkclz.core.exception.ValidationException;
import com.wkclz.dynamicdb.DynamicDataSourceHolder;
import com.wkclz.micro.dbview.bean.dto.ColumnInfo;
import com.wkclz.micro.dbview.bean.dto.IndexInfo;
import com.wkclz.micro.dbview.bean.dto.TableDetail;
import com.wkclz.micro.dbview.bean.dto.TableInfo;
import com.wkclz.micro.dbview.bean.entity.DbviewDatasource;
import com.wkclz.micro.dbview.config.DbviewConfig;
import com.wkclz.micro.dbview.mapper.DbviewDatasourceMapper;
import com.wkclz.mybatis.bean.DataSourceInfo;
import com.wkclz.redis.helper.RedisHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@Service
public class DbviewMetadataService {

    private static final Logger log = LoggerFactory.getLogger(DbviewMetadataService.class);

    private static final String CACHE_PREFIX = "dbview:metadata:";

    @Autowired
    private DbviewDatasourceMapper datasourceMapper;

    @Autowired
    private DbviewConnectionService connectionService;

    @Autowired
    private DbviewConfig dbviewConfig;

    @Autowired
    private RedisHelper redisHelper;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @SuppressWarnings("unchecked")
    public List<String> getSchemas(Long datasourceId) {
        String cacheKey = CACHE_PREFIX + datasourceId + ":schemas";
        Object cached = redisHelper.get(cacheKey);
        if (cached != null) {
            return (List<String>) cached;
        }
        List<String> schemas = executeOnDatasource(datasourceId, conn -> {
            List<String> list = new ArrayList<>();
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery("SHOW DATABASES")) {
                while (rs.next()) {
                    list.add(rs.getString(1));
                }
            }
            return list;
        });
        redisHelper.set(cacheKey, schemas, dbviewConfig.getMetadataCacheTtl(), TimeUnit.SECONDS);
        return schemas;
    }

    @SuppressWarnings("unchecked")
    public List<TableInfo> getTables(Long datasourceId, String schemaName) {
        String cacheKey = CACHE_PREFIX + datasourceId + ":tables:" + schemaName;
        Object cached = redisHelper.get(cacheKey);
        if (cached != null) {
            return (List<TableInfo>) cached;
        }
        final String finalSchemaName = schemaName;
        List<TableInfo> tables = executeOnDatasource(datasourceId, conn -> {
            List<TableInfo> list = new ArrayList<>();
            String schema = finalSchemaName != null ? finalSchemaName : conn.getCatalog();
            String sql = "SELECT TABLE_NAME, TABLE_COMMENT, ENGINE, TABLE_ROWS, DATA_LENGTH, CREATE_TIME, UPDATE_TIME " +
                    "FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_SCHEMA = ?";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, schema);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        TableInfo info = new TableInfo();
                        info.setTableName(rs.getString("TABLE_NAME"));
                        info.setTableComment(rs.getString("TABLE_COMMENT"));
                        info.setEngine(rs.getString("ENGINE"));
                        info.setTableRows(rs.getLong("TABLE_ROWS"));
                        info.setDataLength(rs.getLong("DATA_LENGTH"));
                        info.setCreateTime(rs.getString("CREATE_TIME"));
                        info.setUpdateTime(rs.getString("UPDATE_TIME"));
                        list.add(info);
                    }
                }
            }
            return list;
        });
        redisHelper.set(cacheKey, tables, dbviewConfig.getMetadataCacheTtl(), TimeUnit.SECONDS);
        return tables;
    }

    @SuppressWarnings("unchecked")
    public List<ColumnInfo> getColumns(Long datasourceId, String schemaName, String tableName) {
        String cacheKey = CACHE_PREFIX + datasourceId + ":columns:" + schemaName + ":" + tableName;
        Object cached = redisHelper.get(cacheKey);
        if (cached != null) {
            return (List<ColumnInfo>) cached;
        }
        final String finalSchemaName = schemaName;
        List<ColumnInfo> columns = executeOnDatasource(datasourceId, conn -> {
            List<ColumnInfo> list = new ArrayList<>();
            String schema = finalSchemaName != null ? finalSchemaName : conn.getCatalog();
            String sql = "SELECT TABLE_NAME, COLUMN_NAME, COLUMN_TYPE, COLUMN_COMMENT, IS_NULLABLE, " +
                    "COLUMN_DEFAULT, COLUMN_KEY, ORDINAL_POSITION, EXTRA " +
                    "FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = ? AND TABLE_NAME = ? " +
                    "ORDER BY ORDINAL_POSITION";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, schema);
                ps.setString(2, tableName);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        ColumnInfo info = new ColumnInfo();
                        info.setTableName(rs.getString("TABLE_NAME"));
                        info.setColumnName(rs.getString("COLUMN_NAME"));
                        info.setColumnType(rs.getString("COLUMN_TYPE"));
                        info.setColumnComment(rs.getString("COLUMN_COMMENT"));
                        info.setNullable("YES".equals(rs.getString("IS_NULLABLE")));
                        info.setDefaultValue(rs.getString("COLUMN_DEFAULT"));
                        info.setIsPrimaryKey("PRI".equals(rs.getString("COLUMN_KEY")));
                        info.setOrdinalPosition(rs.getInt("ORDINAL_POSITION"));
                        info.setExtra(rs.getString("EXTRA"));
                        list.add(info);
                    }
                }
            }
            return list;
        });
        redisHelper.set(cacheKey, columns, dbviewConfig.getMetadataCacheTtl(), TimeUnit.SECONDS);
        return columns;
    }

    @SuppressWarnings("unchecked")
    public List<IndexInfo> getIndexes(Long datasourceId, String schemaName, String tableName) {
        String cacheKey = CACHE_PREFIX + datasourceId + ":indexes:" + schemaName + ":" + tableName;
        Object cached = redisHelper.get(cacheKey);
        if (cached != null) {
            return (List<IndexInfo>) cached;
        }
        List<IndexInfo> indexes = executeOnDatasource(datasourceId, conn -> {
            List<IndexInfo> list = new ArrayList<>();
            String qualifiedTable = (schemaName != null ? "`" + schemaName + "`." : "") + "`" + tableName + "`";
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery("SHOW INDEX FROM " + qualifiedTable)) {
                while (rs.next()) {
                    IndexInfo info = new IndexInfo();
                    info.setIndexName(rs.getString("Key_name"));
                    info.setColumnName(rs.getString("Column_name"));
                    info.setNonUnique(rs.getInt("Non_unique") == 1);
                    info.setIndexType(rs.getString("Index_type"));
                    info.setComment(rs.getString("Index_comment"));
                    info.setSeqInIndex(rs.getInt("Seq_in_index"));
                    list.add(info);
                }
            }
            return list;
        });
        redisHelper.set(cacheKey, indexes, dbviewConfig.getMetadataCacheTtl(), TimeUnit.SECONDS);
        return indexes;
    }

    public String getTableDdl(Long datasourceId, String schemaName, String tableName) {
        String cacheKey = CACHE_PREFIX + datasourceId + ":ddl:" + schemaName + ":" + tableName;
        Object cached = redisHelper.get(cacheKey);
        if (cached != null) {
            return (String) cached;
        }
        String ddl = executeOnDatasource(datasourceId, conn -> {
            String qualifiedTable = (schemaName != null ? "`" + schemaName + "`." : "") + "`" + tableName + "`";
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery("SHOW CREATE TABLE " + qualifiedTable)) {
                if (rs.next()) {
                    return rs.getString(2);
                }
            }
            return null;
        });
        if (ddl != null) {
            redisHelper.set(cacheKey, ddl, dbviewConfig.getMetadataCacheTtl(), TimeUnit.SECONDS);
        }
        return ddl;
    }

    public TableDetail getTableDetail(Long datasourceId, String schemaName, String tableName) {
        List<TableInfo> tables = getTables(datasourceId, schemaName);
        TableInfo matchTable = null;
        if (tables != null) {
            for (TableInfo t : tables) {
                if (tableName.equals(t.getTableName())) {
                    matchTable = t;
                    break;
                }
            }
        }
        TableDetail detail = new TableDetail();
        detail.setTableInfo(matchTable);
        detail.setColumns(getColumns(datasourceId, schemaName, tableName));
        detail.setIndexes(getIndexes(datasourceId, schemaName, tableName));
        detail.setDdl(getTableDdl(datasourceId, schemaName, tableName));
        return detail;
    }

    public void refreshCache(Long datasourceId) {
        String pattern = CACHE_PREFIX + datasourceId + ":*";
        Set<String> keys = redisTemplate.keys(pattern);
        if (keys != null && !keys.isEmpty()) {
            redisHelper.delete(keys);
        }
    }

    private <T> T executeOnDatasource(Long datasourceId, ThrowingFunction<Connection, T> action) {
        DbviewDatasource ds = datasourceMapper.selectById(datasourceId);
        if (ds == null) {
            throw ValidationException.of("数据源不存在: " + datasourceId);
        }
        String password = connectionService.decryptPassword(ds.getPassword());
        DataSourceInfo info = new DataSourceInfo();
        info.setUrl(ds.getJdbcUrl());
        info.setUsername(ds.getUsername());
        info.setPassword(password);

        return CompletableFuture.supplyAsync(() -> {
            DynamicDataSourceHolder.set(String.valueOf(datasourceId));
            try (Connection conn = DataSourceInfo.getConnect(info)) {
                return action.apply(conn);
            } catch (SQLException e) {
                throw new RuntimeException("数据库操作失败: " + e.getMessage(), e);
            } finally {
                DynamicDataSourceHolder.clear();
            }
        }).join();
    }

    @FunctionalInterface
    public interface ThrowingFunction<T, R> {
        R apply(T t) throws SQLException;
    }
}
