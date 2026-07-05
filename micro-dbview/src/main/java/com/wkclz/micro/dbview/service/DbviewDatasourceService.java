package com.wkclz.micro.dbview.service;

import com.wkclz.core.base.PageData;
import com.wkclz.core.enums.ResultCode;
import com.wkclz.core.exception.UserException;
import com.wkclz.core.exception.ValidationException;
import com.wkclz.micro.dbview.bean.entity.DbviewDatasource;
import com.wkclz.micro.dbview.mapper.DbviewDatasourceMapper;
import com.wkclz.mybatis.helper.PageQuery;
import com.wkclz.mybatis.service.BaseService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DbviewDatasourceService extends BaseService<DbviewDatasource, DbviewDatasourceMapper> {

    @Autowired
    private DbviewConnectionService connectionService;

    public PageData<DbviewDatasource> getDatasourcePage(DbviewDatasource entity) {
        return PageQuery.page(entity, mapper::getDatasourceList);
    }

    public List<String> getDatasourceOptions() {
        return mapper.getDatasourceOptions();
    }

    public DbviewDatasource create(DbviewDatasource entity) {
        duplicateCheck(entity);
        if (entity.getPort() == null) {
            entity.setPort(3306);
        }
        if (entity.getStatus() == null) {
            entity.setStatus(1);
        }
        if (StringUtils.isBlank(entity.getJdbcUrl()) && entity.getHost() != null) {
            entity.setJdbcUrl(buildJdbcUrl(entity.getHost(), entity.getPort(), entity.getDatabaseName()));
        }
        entity.setPassword(connectionService.encryptPassword(entity.getPassword()));
        mapper.insert(entity);
        return entity;
    }

    public DbviewDatasource update(DbviewDatasource entity) {
        duplicateCheck(entity);
        DbviewDatasource oldEntity = selectById(entity.getId());
        if (oldEntity == null) {
            throw ValidationException.of(ResultCode.RECORD_NOT_EXIST);
        }
        if (StringUtils.isBlank(entity.getJdbcUrl()) && entity.getHost() != null) {
            entity.setJdbcUrl(buildJdbcUrl(entity.getHost(), entity.getPort(), entity.getDatabaseName()));
        }
        if (entity.getPassword() != null && !entity.getPassword().equals(oldEntity.getPassword())) {
            entity.setPassword(connectionService.encryptPassword(entity.getPassword()));
        }
        DbviewDatasource.copyIfNotNull(entity, oldEntity);
        updateByIdSelective(oldEntity);
        connectionService.destroyDatasource(entity.getId());
        return oldEntity;
    }

    public DbviewDatasource remove(DbviewDatasource entity) {
        DbviewDatasource oldEntity = selectById(entity.getId());
        if (oldEntity == null) {
            throw ValidationException.of(ResultCode.RECORD_NOT_EXIST);
        }
        deleteById(oldEntity);
        connectionService.destroyDatasource(entity.getId());
        return oldEntity;
    }

    private void duplicateCheck(DbviewDatasource entity) {
        if (StringUtils.isBlank(entity.getDatasourceName())) {
            return;
        }
        DbviewDatasource param = new DbviewDatasource();
        param.setDatasourceName(entity.getDatasourceName());
        DbviewDatasource existing = mapper.selectByDatasourceName(entity.getDatasourceName());
        if (existing == null) {
            return;
        }
        if (existing.getId().equals(entity.getId())) {
            return;
        }
        throw UserException.of(ResultCode.RECORD_DUPLICATE);
    }

    private String buildJdbcUrl(String host, Integer port, String databaseName) {
        StringBuilder sb = new StringBuilder("jdbc:mysql://");
        sb.append(host);
        if (port != null) {
            sb.append(":").append(port);
        }
        if (StringUtils.isNotBlank(databaseName)) {
            sb.append("/").append(databaseName);
        }
        sb.append("?useUnicode=true&characterEncoding=utf-8&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Asia/Shanghai");
        return sb.toString();
    }
}
