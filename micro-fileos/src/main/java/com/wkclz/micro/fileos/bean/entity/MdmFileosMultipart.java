package com.wkclz.micro.fileos.bean.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import com.wkclz.core.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

@Data
@EqualsAndHashCode(callSuper = true)
public class MdmFileosMultipart extends BaseEntity {

    @Schema(description = "租户编码")
    private String tenantCode;

    @Schema(description = "分片上传ID")
    private String uploadId;

    @Schema(description = "文件存储路径")
    private String fileId;

    @Schema(description = "原始文件名")
    private String fileName;

    @Schema(description = "文件大小")
    private Long fileSize;

    @Schema(description = "MIME类型")
    private String contentType;

    @Schema(description = "业务分类")
    private String category;

    @Schema(description = "是否公共读")
    private Integer isPublic;

    @Schema(description = "OSS服务商")
    private String ossSp;

    @Schema(description = "所属Bucket")
    private String bucketName;

    @Schema(description = "分片总数")
    private Integer partCount;

    @Schema(description = "已完成分片信息")
    private String completedParts;

    @Schema(description = "状态")
    private String status;

    @Schema(description = "过期时间")
    private Date expireTime;

    public static MdmFileosMultipart copy(MdmFileosMultipart source, MdmFileosMultipart target) {
        if (target == null) { target = new MdmFileosMultipart(); }
        if (source == null) { return target; }
        target.setId(source.getId());
        target.setTenantCode(source.getTenantCode());
        target.setUploadId(source.getUploadId());
        target.setFileId(source.getFileId());
        target.setFileName(source.getFileName());
        target.setFileSize(source.getFileSize());
        target.setContentType(source.getContentType());
        target.setCategory(source.getCategory());
        target.setIsPublic(source.getIsPublic());
        target.setOssSp(source.getOssSp());
        target.setBucketName(source.getBucketName());
        target.setPartCount(source.getPartCount());
        target.setCompletedParts(source.getCompletedParts());
        target.setStatus(source.getStatus());
        target.setExpireTime(source.getExpireTime());
        target.setSort(source.getSort());
        target.setCreateTime(source.getCreateTime());
        target.setCreateBy(source.getCreateBy());
        target.setUpdateTime(source.getUpdateTime());
        target.setUpdateBy(source.getUpdateBy());
        target.setRemark(source.getRemark());
        target.setVersion(source.getVersion());
        return target;
    }

    public static MdmFileosMultipart copyIfNotNull(MdmFileosMultipart source, MdmFileosMultipart target) {
        if (target == null) { target = new MdmFileosMultipart(); }
        if (source == null) { return target; }
        if (source.getId() != null) { target.setId(source.getId()); }
        if (source.getTenantCode() != null) { target.setTenantCode(source.getTenantCode()); }
        if (source.getUploadId() != null) { target.setUploadId(source.getUploadId()); }
        if (source.getFileId() != null) { target.setFileId(source.getFileId()); }
        if (source.getFileName() != null) { target.setFileName(source.getFileName()); }
        if (source.getFileSize() != null) { target.setFileSize(source.getFileSize()); }
        if (source.getContentType() != null) { target.setContentType(source.getContentType()); }
        if (source.getCategory() != null) { target.setCategory(source.getCategory()); }
        if (source.getIsPublic() != null) { target.setIsPublic(source.getIsPublic()); }
        if (source.getOssSp() != null) { target.setOssSp(source.getOssSp()); }
        if (source.getBucketName() != null) { target.setBucketName(source.getBucketName()); }
        if (source.getPartCount() != null) { target.setPartCount(source.getPartCount()); }
        if (source.getCompletedParts() != null) { target.setCompletedParts(source.getCompletedParts()); }
        if (source.getStatus() != null) { target.setStatus(source.getStatus()); }
        if (source.getExpireTime() != null) { target.setExpireTime(source.getExpireTime()); }
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
