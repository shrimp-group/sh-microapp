package com.wkclz.micro.dbview.mapper;

import com.wkclz.micro.dbview.bean.entity.DbviewSqlHistory;
import com.wkclz.mybatis.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface DbviewSqlHistoryMapper extends BaseMapper<DbviewSqlHistory> {

    List<DbviewSqlHistory> getHistoryList(DbviewSqlHistory entity);
}
