package com.wkclz.micro.dbview.service;

import com.wkclz.core.base.PageData;
import com.wkclz.core.enums.ResultCode;
import com.wkclz.core.exception.UserException;
import com.wkclz.core.exception.ValidationException;
import com.wkclz.core.identity.IdentityContext;
import com.wkclz.micro.dbview.bean.entity.DbviewDatasourcePermission;
import com.wkclz.micro.dbview.bean.enums.PermissionLevel;
import com.wkclz.micro.dbview.bean.enums.SqlType;
import com.wkclz.micro.dbview.mapper.DbviewDatasourcePermissionMapper;
import com.wkclz.mybatis.helper.PageQuery;
import com.wkclz.mybatis.service.BaseService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DbviewDatasourcePermissionService extends BaseService<DbviewDatasourcePermission, DbviewDatasourcePermissionMapper> {

    public PageData<DbviewDatasourcePermission> getPermissionPage(DbviewDatasourcePermission entity) {
        return PageQuery.page(entity, mapper::getPermissionList);
    }

    public DbviewDatasourcePermission create(DbviewDatasourcePermission entity) {
        duplicateCheck(entity);
        validatePermissionLevel(entity.getPermissionLevel());
        mapper.insert(entity);
        return entity;
    }

    public DbviewDatasourcePermission update(DbviewDatasourcePermission entity) {
        validatePermissionLevel(entity.getPermissionLevel());
        DbviewDatasourcePermission oldEntity = selectById(entity.getId());
        if (oldEntity == null) {
            throw ValidationException.of(ResultCode.RECORD_NOT_EXIST);
        }
        DbviewDatasourcePermission.copyIfNotNull(entity, oldEntity);
        updateByIdSelective(oldEntity);
        return oldEntity;
    }

    public DbviewDatasourcePermission remove(DbviewDatasourcePermission entity) {
        DbviewDatasourcePermission oldEntity = selectById(entity.getId());
        if (oldEntity == null) {
            throw ValidationException.of(ResultCode.RECORD_NOT_EXIST);
        }
        deleteById(oldEntity);
        return oldEntity;
    }

    public List<DbviewDatasourcePermission> getMyPermissions() {
        String userCode = IdentityContext.getUserCode();
        if (StringUtils.isBlank(userCode)) {
            return List.of();
        }
        DbviewDatasourcePermission param = new DbviewDatasourcePermission();
        param.setUserCode(userCode);
        return mapper.getPermissionList(param);
    }

    public PermissionLevel getPermissionLevel(Long datasourceId, String userCode) {
        DbviewDatasourcePermission permission = mapper.selectByDatasourceIdAndUserCode(datasourceId, userCode);
        if (permission == null) {
            return null;
        }
        return PermissionLevel.of(permission.getPermissionLevel());
    }

    public void check(Long datasourceId, String sql, Boolean confirmDangerous) {
        String userCode = IdentityContext.getUserCode();
        if (StringUtils.isBlank(userCode)) {
            throw ValidationException.of("用户未登录，无法执行SQL");
        }

        PermissionLevel level = getPermissionLevel(datasourceId, userCode);
        if (level == null) {
            throw ValidationException.of("您没有该数据源的访问权限");
        }

        SqlType sqlType = SqlType.parse(sql);
        if (sqlType == SqlType.OTHER) {
            throw ValidationException.of("不支持的SQL类型");
        }

        if (!level.isAllowed(sqlType)) {
            throw ValidationException.of("您没有执行 " + sqlType.name() + " 类型SQL的权限，当前权限等级: " + level.name());
        }

        if (isDangerous(sql) && !Boolean.TRUE.equals(confirmDangerous)) {
            throw ValidationException.of("该SQL为危险操作，请确认后执行");
        }
    }

    public boolean isDangerous(String sql) {
        String upper = sql.trim().toUpperCase();
        if ((upper.startsWith("UPDATE") || upper.startsWith("DELETE")) && !upper.contains("WHERE")) {
            return true;
        }
        if (upper.startsWith("DROP TABLE")) {
            return true;
        }
        if (upper.startsWith("TRUNCATE")) {
            return true;
        }
        return false;
    }

    private void duplicateCheck(DbviewDatasourcePermission entity) {
        if (entity.getDatasourceId() == null || StringUtils.isBlank(entity.getUserCode())) {
            return;
        }
        DbviewDatasourcePermission existing = mapper.selectByDatasourceIdAndUserCode(entity.getDatasourceId(), entity.getUserCode());
        if (existing == null) {
            return;
        }
        if (existing.getId().equals(entity.getId())) {
            return;
        }
        throw UserException.of(ResultCode.RECORD_DUPLICATE);
    }

    private void validatePermissionLevel(String permissionLevel) {
        if (PermissionLevel.of(permissionLevel) == null) {
            throw ValidationException.of("无效的权限等级: " + permissionLevel + "，可选值: READ_ONLY, READ_WRITE, DDL");
        }
    }
}
