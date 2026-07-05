package com.wkclz.micro.material.bean.entity;

import com.wkclz.core.annotation.FieldDesc;
import com.wkclz.core.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;




/**
 * Description Create by sh-generator
 * @author shrimp
 * @table mdm_material_transfer_log (素材转移日志) 重新生成代码会覆盖
 */

@Data
@EqualsAndHashCode(callSuper = false)
public class MdmMaterialTransferLog extends BaseEntity {

    /**
     * 素材编码
     */
    @FieldDesc(value = "素材编码", notNull = true)
    private String materialCode;

    /**
     * 原所有者用户编码
     */
    @FieldDesc(value = "原所有者用户编码", notNull = true)
    private String fromUserCode;

    /**
     * 新所有者用户编码
     */
    @FieldDesc(value = "新所有者用户编码", notNull = true)
    private String toUserCode;

    /**
     * 操作人用户编码
     */
    @FieldDesc(value = "操作人用户编码", notNull = true)
    private String operatorCode;

    /**
     * 操作人用户编码
     */
    @FieldDesc(value = "操作人用户编码", notNull = true)
    private String userCode;

    /**
     * 租户编码
     */
    @FieldDesc(value = "租户编码", notNull = true)
    private String tenantCode;


    public static MdmMaterialTransferLog copy(MdmMaterialTransferLog source, MdmMaterialTransferLog target) {
        if (target == null ) { target = new MdmMaterialTransferLog();}
        if (source == null) { return target; }
        target.setId(source.getId());
        target.setMaterialCode(source.getMaterialCode());
        target.setFromUserCode(source.getFromUserCode());
        target.setToUserCode(source.getToUserCode());
        target.setOperatorCode(source.getOperatorCode());
        target.setUserCode(source.getUserCode());
        target.setTenantCode(source.getTenantCode());
        target.setSort(source.getSort());
        target.setCreateTime(source.getCreateTime());
        target.setCreateBy(source.getCreateBy());
        target.setUpdateTime(source.getUpdateTime());
        target.setUpdateBy(source.getUpdateBy());
        target.setRemark(source.getRemark());
        target.setVersion(source.getVersion());
        return target;
    }

    public static MdmMaterialTransferLog copyIfNotNull(MdmMaterialTransferLog source, MdmMaterialTransferLog target) {
        if (target == null ) { target = new MdmMaterialTransferLog();}
        if (source == null) { return target; }
        if (source.getId() != null) { target.setId(source.getId()); }
        if (source.getMaterialCode() != null) { target.setMaterialCode(source.getMaterialCode()); }
        if (source.getFromUserCode() != null) { target.setFromUserCode(source.getFromUserCode()); }
        if (source.getToUserCode() != null) { target.setToUserCode(source.getToUserCode()); }
        if (source.getOperatorCode() != null) { target.setOperatorCode(source.getOperatorCode()); }
        if (source.getUserCode() != null) { target.setUserCode(source.getUserCode()); }
        if (source.getTenantCode() != null) { target.setTenantCode(source.getTenantCode()); }
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

