package com.wkclz.micro.dbview.service;

import com.wkclz.dynamicdb.DynamicDataSource;
import com.wkclz.dynamicdb.DynamicDataSourceHolder;
import com.wkclz.micro.dbview.bean.entity.DbviewDatasource;
import com.wkclz.micro.dbview.config.DbviewConfig;
import com.wkclz.micro.dbview.mapper.DbviewDatasourceMapper;
import com.wkclz.mybatis.bean.DataSourceInfo;
import com.wkclz.spring.config.SpringContextHolder;
import com.wkclz.tool.tools.AesTool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

@Service
public class DbviewConnectionService {

    private static final Logger log = LoggerFactory.getLogger(DbviewConnectionService.class);

    @Autowired
    private DbviewDatasourceMapper datasourceMapper;

    @Autowired
    private DbviewConfig dbviewConfig;

    public <T> T executeOnDatasource(Long datasourceId, Supplier<T> action) {
        return CompletableFuture.supplyAsync(() -> {
            DynamicDataSourceHolder.set(String.valueOf(datasourceId));
            try {
                return action.get();
            } finally {
                DynamicDataSourceHolder.clear();
            }
        }).join();
    }

    public boolean testConnection(Long datasourceId) {
        DbviewDatasource ds = datasourceMapper.selectById(datasourceId);
        if (ds == null) {
            return false;
        }
        String password = decryptPassword(ds.getPassword());
        DataSourceInfo info = new DataSourceInfo();
        info.setUrl(ds.getJdbcUrl());
        info.setUsername(ds.getUsername());
        info.setPassword(password);
        try {
            Connection conn = DataSourceInfo.getConnect(info);
            if (conn != null) {
                conn.close();
                return true;
            }
        } catch (SQLException e) {
            log.warn("测试数据源连接失败: datasourceId={}, error={}", datasourceId, e.getMessage());
        }
        return false;
    }

    public boolean testConnection(DbviewDatasource ds) {
        String password = decryptPassword(ds.getPassword());
        DataSourceInfo info = new DataSourceInfo();
        info.setUrl(ds.getJdbcUrl());
        info.setUsername(ds.getUsername());
        info.setPassword(password);
        try {
            Connection conn = DataSourceInfo.getConnect(info);
            if (conn != null) {
                conn.close();
                return true;
            }
        } catch (SQLException e) {
            log.warn("测试数据源连接失败: {}", e.getMessage());
        }
        return false;
    }

    public void destroyDatasource(Long datasourceId) {
        try {
            DynamicDataSource ds = SpringContextHolder.getBean(DynamicDataSource.class);
            ds.destroyDataSource(String.valueOf(datasourceId));
        } catch (Exception e) {
            log.warn("销毁数据源连接池失败: datasourceId={}, error={}", datasourceId, e.getMessage());
        }
    }

    public String encryptPassword(String password) {
        String aesKey = dbviewConfig.getAesKey();
        if (aesKey == null || aesKey.isBlank()) {
            return password;
        }
        return AesTool.encrypt(password, aesKey);
    }

    public String decryptPassword(String encryptedPassword) {
        String aesKey = dbviewConfig.getAesKey();
        if (aesKey == null || aesKey.isBlank()) {
            return encryptedPassword;
        }
        return AesTool.decrypt(encryptedPassword, aesKey);
    }
}
