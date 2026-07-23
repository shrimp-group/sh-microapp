package com.wkclz.micro.fileos.bean.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import com.wkclz.core.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class MdmFileosDirectory extends BaseEntity {

    @Schema(description = "租户编码")
    private String tenantCode;

    @Schema(description = "所属Bucket")
    private String bucketName;

    @Schema(description = "目录完整路径")
    private String dirPath;

    @Schema(description = "目录名")
    private String dirName;

    @Schema(description = "父目录路径")
    private String parentPath;

    @Schema(description = "目录层级")
    private Integer dirLevel;

    @Schema(description = "文件数量")
    private Long fileCount;

    @Schema(description = "文件总大小")
    private Long totalSize;

    public static MdmFileosDirectory copy(MdmFileosDirectory source, MdmFileosDirectory target) {
        if (target == null) { target = new MdmFileosDirectory(); }
        if (source == null) { return target; }
        target.setId(source.getId());
        target.setTenantCode(source.getTenantCode());
        target.setBucketName(source.getBucketName());
        target.setDirPath(source.getDirPath());
        target.setDirName(source.getDirName());
        target.setParentPath(source.getParentPath());
        target.setDirLevel(source.getDirLevel());
        target.setFileCount(source.getFileCount());
        target.setTotalSize(source.getTotalSize());
        target.setSort(source.getSort());
        target.setCreateTime(source.getCreateTime());
        target.setCreateBy(source.getCreateBy());
        target.setUpdateTime(source.getUpdateTime());
        target.setUpdateBy(source.getUpdateBy());
        target.setRemark(source.getRemark());
        target.setVersion(source.getVersion());
        return target;
    }

    public static MdmFileosDirectory copyIfNotNull(MdmFileosDirectory source, MdmFileosDirectory target) {
        if (target == null) { target = new MdmFileosDirectory(); }
        if (source == null) { return target; }
        if (source.getId() != null) { target.setId(source.getId()); }
        if (source.getTenantCode() != null) { target.setTenantCode(source.getTenantCode()); }
        if (source.getBucketName() != null) { target.setBucketName(source.getBucketName()); }
        if (source.getDirPath() != null) { target.setDirPath(source.getDirPath()); }
        if (source.getDirName() != null) { target.setDirName(source.getDirName()); }
        if (source.getParentPath() != null) { target.setParentPath(source.getParentPath()); }
        if (source.getDirLevel() != null) { target.setDirLevel(source.getDirLevel()); }
        if (source.getFileCount() != null) { target.setFileCount(source.getFileCount()); }
        if (source.getTotalSize() != null) { target.setTotalSize(source.getTotalSize()); }
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
