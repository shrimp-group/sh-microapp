package com.wkclz.micro.dbview.service;

import com.wkclz.core.base.PageData;
import com.wkclz.core.exception.ValidationException;
import com.wkclz.core.user.UserContext;
import com.wkclz.dynamicdb.DynamicDataSourceHolder;
import com.wkclz.micro.dbview.bean.dto.SqlExecuteRequest;
import com.wkclz.micro.dbview.bean.dto.SqlResult;
import com.wkclz.micro.dbview.bean.entity.DbviewDatasource;
import com.wkclz.micro.dbview.bean.entity.DbviewSqlHistory;
import com.wkclz.micro.dbview.bean.enums.SqlType;
import com.wkclz.micro.dbview.config.DbviewConfig;
import com.wkclz.micro.dbview.mapper.DbviewDatasourceMapper;
import com.wkclz.mybatis.bean.DataSourceInfo;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.*;
import java.util.*;
import java.util.concurrent.CompletableFuture;

@Service
public class DbviewSqlService {

    private static final Logger log = LoggerFactory.getLogger(DbviewSqlService.class);

    @Autowired
    private DbviewDatasourceMapper datasourceMapper;

    @Autowired
    private DbviewConnectionService connectionService;

    @Autowired
    private DbviewDatasourcePermissionService permissionService;

    @Autowired
    private DbviewSqlHistoryService sqlHistoryService;

    @Autowired
    private DbviewConfig dbviewConfig;

    public SqlResult execute(SqlExecuteRequest request) {
        Long datasourceId = request.getDatasourceId();
        String sql = request.getSql().trim();

        if (StringUtils.isBlank(sql)) {
            throw ValidationException.of("SQL不能为空");
        }

        if (containsMultipleStatements(sql)) {
            throw ValidationException.of("一次只能执行一条SQL语句");
        }

        permissionService.check(datasourceId, sql, request.getConfirmDangerous());

        SqlType sqlType = SqlType.parse(sql);
        int maxRows = Math.min(
                request.getMaxRows() != null ? request.getMaxRows() : dbviewConfig.getMaxRows(),
                dbviewConfig.getMaxRowsLimit()
        );

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
        String userCode = UserContext.getUserCode();

        try {
            SqlResult result = CompletableFuture.supplyAsync(() -> {
                DynamicDataSourceHolder.set(String.valueOf(datasourceId));
                try (Connection conn = DataSourceInfo.getConnect(info)) {
                    return doExecute(conn, sql, sqlType, maxRows);
                } catch (SQLException e) {
                    throw new RuntimeException(e.getMessage(), e);
                } finally {
                    DynamicDataSourceHolder.clear();
                }
            }).join();

            long costMs = System.currentTimeMillis() - startTime;
            result.setCostMs(costMs);
            result.setSqlType(sqlType.name());

            sqlHistoryService.insertHistory(datasourceId, userCode, sql, sqlType.name(),
                    1, result.getAffectedRows(), costMs, null);

            return result;
        } catch (Exception e) {
            long costMs = System.currentTimeMillis() - startTime;
            String errorMsg = e.getCause() != null ? e.getCause().getMessage() : e.getMessage();
            sqlHistoryService.insertHistory(datasourceId, userCode, sql, sqlType.name(),
                    0, 0L, costMs, errorMsg);
            throw ValidationException.of("SQL执行失败: " + errorMsg);
        }
    }

    public PageData<DbviewSqlHistory> getHistoryPage(DbviewSqlHistory query) {
        return sqlHistoryService.getHistoryPage(query);
    }

    private SqlResult doExecute(Connection conn, String sql, SqlType sqlType, int maxRows) throws SQLException {
        SqlResult result = new SqlResult();

        if (sqlType.isRead()) {
            try (Statement stmt = conn.createStatement()) {
                stmt.setFetchSize(maxRows + 1);
                stmt.setMaxRows(maxRows + 1);
                try (ResultSet rs = stmt.executeQuery(sql)) {
                    ResultSetMetaData metaData = rs.getMetaData();
                    int columnCount = metaData.getColumnCount();
                    List<String> columns = new ArrayList<>();
                    for (int i = 1; i <= columnCount; i++) {
                        columns.add(metaData.getColumnLabel(i));
                    }
                    result.setColumns(columns);

                    List<Map<String, Object>> rows = new ArrayList<>();
                    int rowCount = 0;
                    while (rs.next() && rowCount < maxRows) {
                        Map<String, Object> row = new LinkedHashMap<>();
                        for (int i = 1; i <= columnCount; i++) {
                            row.put(columns.get(i - 1), rs.getObject(i));
                        }
                        rows.add(row);
                        rowCount++;
                    }
                    result.setRows(rows);
                    result.setTotal((long) rowCount);
                    result.setAffectedRows(0L);

                    if (rs.next()) {
                        result.setTruncated(true);
                    }
                }
            }
        } else if (sqlType.isWrite()) {
            try (Statement stmt = conn.createStatement()) {
                long affectedRows = stmt.executeUpdate(sql);
                result.setColumns(List.of());
                result.setRows(List.of());
                result.setTotal(0L);
                result.setAffectedRows(affectedRows);
            }
        } else if (sqlType.isDdl()) {
            try (Statement stmt = conn.createStatement()) {
                stmt.execute(sql);
                result.setColumns(List.of());
                result.setRows(List.of());
                result.setTotal(0L);
                result.setAffectedRows(0L);
            }
        } else {
            try (Statement stmt = conn.createStatement()) {
                boolean hasResultSet = stmt.execute(sql);
                if (hasResultSet) {
                    try (ResultSet rs = stmt.getResultSet()) {
                        ResultSetMetaData metaData = rs.getMetaData();
                        int columnCount = metaData.getColumnCount();
                        List<String> columns = new ArrayList<>();
                        for (int i = 1; i <= columnCount; i++) {
                            columns.add(metaData.getColumnLabel(i));
                        }
                        result.setColumns(columns);
                        List<Map<String, Object>> rows = new ArrayList<>();
                        while (rs.next()) {
                            Map<String, Object> row = new LinkedHashMap<>();
                            for (int i = 1; i <= columnCount; i++) {
                                row.put(columns.get(i - 1), rs.getObject(i));
                            }
                            rows.add(row);
                        }
                        result.setRows(rows);
                        result.setTotal((long) rows.size());
                    }
                } else {
                    result.setColumns(List.of());
                    result.setRows(List.of());
                    result.setTotal(0L);
                    result.setAffectedRows((long) stmt.getUpdateCount());
                }
            }
        }

        return result;
    }

    private boolean containsMultipleStatements(String sql) {
        String trimmed = sql.trim();
        if (trimmed.endsWith(";")) {
            trimmed = trimmed.substring(0, trimmed.length() - 1).trim();
        }
        return trimmed.contains(";") && !trimmed.endsWith("\"") && !trimmed.endsWith("'");
    }
}
