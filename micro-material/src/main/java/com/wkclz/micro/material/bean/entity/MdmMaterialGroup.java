package com.wkclz.micro.material.bean.entity;

import com.wkclz.core.annotation.FieldDesc;
import com.wkclz.core.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;




/**
 * Description Create by sh-generator
 * @author shrimp
 * @table mdm_material_group (素材分组) 重新生成代码会覆盖
 */

@Data
@EqualsAndHashCode(callSuper = false)
public class MdmMaterialGroup extends BaseEntity {

    /**
     * 分组编码
     */
    @FieldDesc(value = "分组编码", notNull = true)
    private String groupCode;

    /**
     * 父级分组编码(顶级为0)
     */
    @FieldDesc(value = "父级分组编码(顶级为0)", notNull = true)
    private String parentCode;

    /**
     * 分组名称
     */
    @FieldDesc(value = "分组名称", notNull = true)
    private String groupName;

    /**
     * 分组类型(SYSTEM/PERSONAL)
     */
    @FieldDesc(value = "分组类型(SYSTEM/PERSONAL)", notNull = true)
    private String groupType;

    /**
     * 所有者用户编码
     */
    @FieldDesc(value = "所有者用户编码", notNull = true)
    private String userCode;

    /**
     * 租户编码
     */
    @FieldDesc(value = "租户编码", notNull = true)
    private String tenantCode;


    public static MdmMaterialGroup copy(MdmMaterialGroup source, MdmMaterialGroup target) {
        if (target == null ) { target = new MdmMaterialGroup();}
        if (source == null) { return target; }
        target.setId(source.getId());
        target.setGroupCode(source.getGroupCode());
        target.setParentCode(source.getParentCode());
        target.setGroupName(source.getGroupName());
        target.setGroupType(source.getGroupType());
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

    public static MdmMaterialGroup copyIfNotNull(MdmMaterialGroup source, MdmMaterialGroup target) {
        if (target == null ) { target = new MdmMaterialGroup();}
        if (source == null) { return target; }
        if (source.getId() != null) { target.setId(source.getId()); }
        if (source.getGroupCode() != null) { target.setGroupCode(source.getGroupCode()); }
        if (source.getParentCode() != null) { target.setParentCode(source.getParentCode()); }
        if (source.getGroupName() != null) { target.setGroupName(source.getGroupName()); }
        if (source.getGroupType() != null) { target.setGroupType(source.getGroupType()); }
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

