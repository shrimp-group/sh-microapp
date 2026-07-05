package com.wkclz.micro.dbview.mapper;

import com.wkclz.micro.dbview.bean.entity.DbviewDatasourcePermission;
import com.wkclz.mybatis.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface DbviewDatasourcePermissionMapper extends BaseMapper<DbviewDatasourcePermission> {

    List<DbviewDatasourcePermission> getPermissionList(DbviewDatasourcePermission entity);

    DbviewDatasourcePermission selectByDatasourceIdAndUserCode(Long datasourceId, String userCode);
}
