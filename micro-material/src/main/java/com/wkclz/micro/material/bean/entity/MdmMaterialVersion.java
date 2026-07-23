package com.wkclz.micro.material.bean.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import com.wkclz.core.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;




/**
 * Description Create by sh-generator
 * @author shrimp
 * @table mdm_material_version (素材版本) 重新生成代码会覆盖
 */

@Data
@EqualsAndHashCode(callSuper = false)
public class MdmMaterialVersion extends BaseEntity {

    /**
     * 素材编码
     */
    @Schema(description = "素材编码")
    private String materialCode;

    /**
     * 版本号
     */
    @Schema(description = "版本号")
    private Integer versionNo;

    /**
     * 文件ID
     */
    @Schema(description = "文件ID")
    private String fileId;

    /**
     * 文件名
     */
    @Schema(description = "文件名")
    private String fileName;

    /**
     * 文件大小(字节)
     */
    @Schema(description = "文件大小(字节)")
    private Long fileSize;

    /**
     * 操作人用户编码
     */
    @Schema(description = "操作人用户编码")
    private String userCode;

    /**
     * 租户编码
     */
    @Schema(description = "租户编码")
    private String tenantCode;


    public static MdmMaterialVersion copy(MdmMaterialVersion source, MdmMaterialVersion target) {
        if (target == null ) { target = new MdmMaterialVersion();}
        if (source == null) { return target; }
        target.setId(source.getId());
        target.setMaterialCode(source.getMaterialCode());
        target.setVersionNo(source.getVersionNo());
        target.setFileId(source.getFileId());
        target.setFileName(source.getFileName());
        target.setFileSize(source.getFileSize());
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

    public static MdmMaterialVersion copyIfNotNull(MdmMaterialVersion source, MdmMaterialVersion target) {
        if (target == null ) { target = new MdmMaterialVersion();}
        if (source == null) { return target; }
        if (source.getId() != null) { target.setId(source.getId()); }
        if (source.getMaterialCode() != null) { target.setMaterialCode(source.getMaterialCode()); }
        if (source.getVersionNo() != null) { target.setVersionNo(source.getVersionNo()); }
        if (source.getFileId() != null) { target.setFileId(source.getFileId()); }
        if (source.getFileName() != null) { target.setFileName(source.getFileName()); }
        if (source.getFileSize() != null) { target.setFileSize(source.getFileSize()); }
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

