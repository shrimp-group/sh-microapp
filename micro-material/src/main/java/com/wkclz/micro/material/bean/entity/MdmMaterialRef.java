package com.wkclz.micro.material.bean.entity;

import com.wkclz.core.annotation.FieldDesc;
import com.wkclz.core.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;




/**
 * Description Create by sh-generator
 * @author shrimp
 * @table mdm_material_ref (素材引用) 重新生成代码会覆盖
 */

@Data
@EqualsAndHashCode(callSuper = false)
public class MdmMaterialRef extends BaseEntity {

    /**
     * 素材编码
     */
    @FieldDesc(value = "素材编码", notNull = true)
    private String materialCode;

    /**
     * 业务类型
     */
    @FieldDesc(value = "业务类型", notNull = true)
    private String bizType;

    /**
     * 业务编码
     */
    @FieldDesc(value = "业务编码", notNull = true)
    private String bizCode;

    /**
     * 引用描述
     */
    @FieldDesc(value = "引用描述")
    private String refDesc;

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


    public static MdmMaterialRef copy(MdmMaterialRef source, MdmMaterialRef target) {
        if (target == null ) { target = new MdmMaterialRef();}
        if (source == null) { return target; }
        target.setId(source.getId());
        target.setMaterialCode(source.getMaterialCode());
        target.setBizType(source.getBizType());
        target.setBizCode(source.getBizCode());
        target.setRefDesc(source.getRefDesc());
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

    public static MdmMaterialRef copyIfNotNull(MdmMaterialRef source, MdmMaterialRef target) {
        if (target == null ) { target = new MdmMaterialRef();}
        if (source == null) { return target; }
        if (source.getId() != null) { target.setId(source.getId()); }
        if (source.getMaterialCode() != null) { target.setMaterialCode(source.getMaterialCode()); }
        if (source.getBizType() != null) { target.setBizType(source.getBizType()); }
        if (source.getBizCode() != null) { target.setBizCode(source.getBizCode()); }
        if (source.getRefDesc() != null) { target.setRefDesc(source.getRefDesc()); }
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

