package com.wkclz.micro.dbview.config;

import com.wkclz.dynamicdb.DynamicDataSourceFactory;
import com.wkclz.micro.dbview.bean.entity.DbviewDatasource;
import com.wkclz.micro.dbview.mapper.DbviewDatasourceMapper;
import com.wkclz.mybatis.bean.DataSourceInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class DbviewDataSourceFactory implements DynamicDataSourceFactory {

    @Autowired
    private DbviewDatasourceMapper datasourceMapper;

    @Override
    public DataSourceInfo createDataSource(String key) {
        Long datasourceId = Long.valueOf(key);
        DbviewDatasource ds = datasourceMapper.selectById(datasourceId);
        if (ds == null) {
            return null;
        }
        DataSourceInfo info = new DataSourceInfo();
        info.setUrl(ds.getJdbcUrl());
        info.setUsername(ds.getUsername());
        info.setPassword(ds.getPassword());
        return info;
    }
}
