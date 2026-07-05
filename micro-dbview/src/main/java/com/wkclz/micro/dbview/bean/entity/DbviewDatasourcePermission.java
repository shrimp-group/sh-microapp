package com.wkclz.micro.dbview.bean.entity;

import com.wkclz.core.annotation.FieldDesc;
import com.wkclz.core.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class DbviewDatasourcePermission extends BaseEntity {

    @FieldDesc("数据源ID")
    private Long datasourceId;

    @FieldDesc("用户编码")
    private String userCode;

    @FieldDesc("权限等级：READ_ONLY / READ_WRITE / DDL")
    private String permissionLevel;

    public static DbviewDatasourcePermission copy(DbviewDatasourcePermission source, DbviewDatasourcePermission target) {
        if (target == null) { target = new DbviewDatasourcePermission(); }
        if (source == null) { return target; }
        target.setId(source.getId());
        target.setDatasourceId(source.getDatasourceId());
        target.setUserCode(source.getUserCode());
        target.setPermissionLevel(source.getPermissionLevel());
        target.setSort(source.getSort());
        target.setCreateTime(source.getCreateTime());
        target.setCreateBy(source.getCreateBy());
        target.setUpdateTime(source.getUpdateTime());
        target.setUpdateBy(source.getUpdateBy());
        target.setRemark(source.getRemark());
        target.setVersion(source.getVersion());
        return target;
    }

    public static DbviewDatasourcePermission copyIfNotNull(DbviewDatasourcePermission source, DbviewDatasourcePermission target) {
        if (target == null) { target = new DbviewDatasourcePermission(); }
        if (source == null) { return target; }
        if (source.getId() != null) { target.setId(source.getId()); }
        if (source.getDatasourceId() != null) { target.setDatasourceId(source.getDatasourceId()); }
        if (source.getUserCode() != null) { target.setUserCode(source.getUserCode()); }
        if (source.getPermissionLevel() != null) { target.setPermissionLevel(source.getPermissionLevel()); }
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
