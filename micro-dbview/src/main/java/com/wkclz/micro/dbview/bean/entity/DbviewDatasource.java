package com.wkclz.micro.dbview.bean.entity;

import com.wkclz.core.annotation.FieldDesc;
import com.wkclz.core.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class DbviewDatasource extends BaseEntity {

    @FieldDesc("数据源名称")
    private String datasourceName;

    @FieldDesc("主机地址")
    private String host;

    @FieldDesc("端口")
    private Integer port;

    @FieldDesc("数据库名")
    private String databaseName;

    @FieldDesc("JDBC URL")
    private String jdbcUrl;

    @FieldDesc("用户名")
    private String username;

    @FieldDesc("密码")
    private String password;

    @FieldDesc("状态：0-禁用 1-启用")
    private Integer status;

    public static DbviewDatasource copy(DbviewDatasource source, DbviewDatasource target) {
        if (target == null) { target = new DbviewDatasource(); }
        if (source == null) { return target; }
        target.setId(source.getId());
        target.setDatasourceName(source.getDatasourceName());
        target.setHost(source.getHost());
        target.setPort(source.getPort());
        target.setDatabaseName(source.getDatabaseName());
        target.setJdbcUrl(source.getJdbcUrl());
        target.setUsername(source.getUsername());
        target.setPassword(source.getPassword());
        target.setStatus(source.getStatus());
        target.setSort(source.getSort());
        target.setCreateTime(source.getCreateTime());
        target.setCreateBy(source.getCreateBy());
        target.setUpdateTime(source.getUpdateTime());
        target.setUpdateBy(source.getUpdateBy());
        target.setRemark(source.getRemark());
        target.setVersion(source.getVersion());
        return target;
    }

    public static DbviewDatasource copyIfNotNull(DbviewDatasource source, DbviewDatasource target) {
        if (target == null) { target = new DbviewDatasource(); }
        if (source == null) { return target; }
        if (source.getId() != null) { target.setId(source.getId()); }
        if (source.getDatasourceName() != null) { target.setDatasourceName(source.getDatasourceName()); }
        if (source.getHost() != null) { target.setHost(source.getHost()); }
        if (source.getPort() != null) { target.setPort(source.getPort()); }
        if (source.getDatabaseName() != null) { target.setDatabaseName(source.getDatabaseName()); }
        if (source.getJdbcUrl() != null) { target.setJdbcUrl(source.getJdbcUrl()); }
        if (source.getUsername() != null) { target.setUsername(source.getUsername()); }
        if (source.getPassword() != null) { target.setPassword(source.getPassword()); }
        if (source.getStatus() != null) { target.setStatus(source.getStatus()); }
        if (source.getSort() != null) { target.setSort(source.getSort()); }
        if (source.getCreateTime() != null) { target.setCreateTime(source.getCreateTime()); }
        if (source.getCreateBy() != null) { target.setCreateBy(source.getCreateBy()); }
        if (source.getUpdateTime() != null) { target.setUpdateTime(source.getUpdateTime()); }
        if (source.getUpdateBy() != null) { target.setUpdateBy(source.getUpdateBy()); }
        if (source.getRemark() != null) { target.setRemark(source.getRemark()); }
        if (source.getVersion() != null) { target.setVersion(source.getVersion()); }
        return target;
    }
}
