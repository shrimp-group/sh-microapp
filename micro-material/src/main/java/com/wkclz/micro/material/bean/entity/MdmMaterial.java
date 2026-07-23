package com.wkclz.micro.material.bean.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import com.wkclz.core.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;




/**
 * Description Create by sh-generator
 * @author shrimp
 * @table mdm_material (素材) 重新生成代码会覆盖
 */

@Data
@EqualsAndHashCode(callSuper = false)
public class MdmMaterial extends BaseEntity {

    /**
     * 素材编码
     */
    @Schema(description = "素材编码")
    private String materialCode;

    /**
     * 素材名称
     */
    @Schema(description = "素材名称")
    private String materialName;

    /**
     * 素材类型(IMAGE/VIDEO/AUDIO/DOCUMENT/OTHER)
     */
    @Schema(description = "素材类型(IMAGE/VIDEO/AUDIO/DOCUMENT/OTHER)")
    private String materialType;

    /**
     * 来源类型(UPLOAD/LINK)
     */
    @Schema(description = "来源类型(UPLOAD/LINK)")
    private String sourceType;

    /**
     * 分组编码
     */
    @Schema(description = "分组编码")
    private String groupCode;

    /**
     * 文件ID(关联micro-fileos)
     */
    @Schema(description = "文件ID(关联micro-fileos)")
    private String fileId;

    /**
     * 文件名
     */
    @Schema(description = "文件名")
    private String fileName;

    /**
     * 文件扩展名
     */
    @Schema(description = "文件扩展名")
    private String fileExt;

    /**
     * 文件大小(字节)
     */
    @Schema(description = "文件大小(字节)")
    private Long fileSize;

    /**
     * 链接地址(来源为LINK时)
     */
    @Schema(description = "链接地址(来源为LINK时)")
    private String linkUrl;

    /**
     * 链接状态(VALID/INVALID/UNKNOWN)
     */
    @Schema(description = "链接状态(VALID/INVALID/UNKNOWN)")
    private String linkStatus;

    /**
     * 可见性(PRIVATE/PUBLIC)
     */
    @Schema(description = "可见性(PRIVATE/PUBLIC)")
    private String visibility;

    /**
     * 封面文件ID
     */
    @Schema(description = "封面文件ID")
    private String coverFileId;

    /**
     * 描述
     */
    @Schema(description = "描述")
    private String description;

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


    public static MdmMaterial copy(MdmMaterial source, MdmMaterial target) {
        if (target == null ) { target = new MdmMaterial();}
        if (source == null) { return target; }
        target.setId(source.getId());
        target.setMaterialCode(source.getMaterialCode());
        target.setMaterialName(source.getMaterialName());
        target.setMaterialType(source.getMaterialType());
        target.setSourceType(source.getSourceType());
        target.setGroupCode(source.getGroupCode());
        target.setFileId(source.getFileId());
        target.setFileName(source.getFileName());
        target.setFileExt(source.getFileExt());
        target.setFileSize(source.getFileSize());
        target.setLinkUrl(source.getLinkUrl());
        target.setLinkStatus(source.getLinkStatus());
        target.setVisibility(source.getVisibility());
        target.setCoverFileId(source.getCoverFileId());
        target.setDescription(source.getDescription());
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

    public static MdmMaterial copyIfNotNull(MdmMaterial source, MdmMaterial target) {
        if (target == null ) { target = new MdmMaterial();}
        if (source == null) { return target; }
        if (source.getId() != null) { target.setId(source.getId()); }
        if (source.getMaterialCode() != null) { target.setMaterialCode(source.getMaterialCode()); }
        if (source.getMaterialName() != null) { target.setMaterialName(source.getMaterialName()); }
        if (source.getMaterialType() != null) { target.setMaterialType(source.getMaterialType()); }
        if (source.getSourceType() != null) { target.setSourceType(source.getSourceType()); }
        if (source.getGroupCode() != null) { target.setGroupCode(source.getGroupCode()); }
        if (source.getFileId() != null) { target.setFileId(source.getFileId()); }
        if (source.getFileName() != null) { target.setFileName(source.getFileName()); }
        if (source.getFileExt() != null) { target.setFileExt(source.getFileExt()); }
        if (source.getFileSize() != null) { target.setFileSize(source.getFileSize()); }
        if (source.getLinkUrl() != null) { target.setLinkUrl(source.getLinkUrl()); }
        if (source.getLinkStatus() != null) { target.setLinkStatus(source.getLinkStatus()); }
        if (source.getVisibility() != null) { target.setVisibility(source.getVisibility()); }
        if (source.getCoverFileId() != null) { target.setCoverFileId(source.getCoverFileId()); }
        if (source.getDescription() != null) { target.setDescription(source.getDescription()); }
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

