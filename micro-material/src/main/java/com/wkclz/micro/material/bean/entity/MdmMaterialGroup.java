package com.wkclz.micro.material.bean.entity;

import io.swagger.v3.oas.annotations.media.Schema;
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
    @Schema(description = "分组编码")
    private String groupCode;

    /**
     * 父级分组编码(顶级为0)
     */
    @Schema(description = "父级分组编码(顶级为0)")
    private String parentCode;

    /**
     * 分组名称
     */
    @Schema(description = "分组名称")
    private String groupName;

    /**
     * 分组类型(SYSTEM/PERSONAL)
     */
    @Schema(description = "分组类型(SYSTEM/PERSONAL)")
    private String groupType;

    /**
     * 所有者用户编码
     */
    @Schema(description = "所有者用户编码")
    private String userCode;

    /**
     * 租户编码
     */
    @Schema(description = "租户编码")
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

