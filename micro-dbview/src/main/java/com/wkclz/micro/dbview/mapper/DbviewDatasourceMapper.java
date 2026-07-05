package com.wkclz.micro.dbview.mapper;

import com.wkclz.micro.dbview.bean.entity.DbviewDatasource;
import com.wkclz.mybatis.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface DbviewDatasourceMapper extends BaseMapper<DbviewDatasource> {

    List<DbviewDatasource> getDatasourceList(DbviewDatasource entity);

    List<String> getDatasourceOptions();

    DbviewDatasource selectByDatasourceName(String datasourceName);
}
