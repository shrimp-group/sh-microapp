package com.wkclz.micro.fileos.bean.entity;

import com.wkclz.core.base.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class MdmFileosRecord extends BaseEntity {

    @Schema(description = "租户编码")
    private String tenantCode;

    @Schema(description = "文件存储路径")
    private String fileId;

    @Schema(description = "原始文件名")
    private String fileName;

    @Schema(description = "文件扩展名")
    private String fileType;

    @Schema(description = "文件大小")
    private Long fileSize;

    @Schema(description = "文件Hash")
    private String fileHash;

    @Schema(description = "MIME类型")
    private String contentType;

    @Schema(description = "业务分类")
    private String category;

    @Schema(description = "所属目录路径")
    private String dirPath;

    @Schema(description = "是否公共读")
    private Integer isPublic;

    @Schema(description = "OSS服务商")
    private String ossSp;

    @Schema(description = "所属Bucket")
    private String bucketName;

    @Schema(description = "上传方式")
    private String uploadType;

    @Schema(description = "分片上传ID")
    private String uploadId;

    @Schema(description = "上传状态")
    private String uploadStatus;

    @Schema(description = "图片处理参数")
    private String imageProcess;

    public static MdmFileosRecord copy(MdmFileosRecord source, MdmFileosRecord target) {
        if (target == null) { target = new MdmFileosRecord(); }
        if (source == null) { return target; }
        target.setId(source.getId());
        target.setTenantCode(source.getTenantCode());
        target.setFileId(source.getFileId());
        target.setFileName(source.getFileName());
        target.setFileType(source.getFileType());
        target.setFileSize(source.getFileSize());
        target.setFileHash(source.getFileHash());
        target.setContentType(source.getContentType());
        target.setCategory(source.getCategory());
        target.setDirPath(source.getDirPath());
        target.setIsPublic(source.getIsPublic());
        target.setOssSp(source.getOssSp());
        target.setBucketName(source.getBucketName());
        target.setUploadType(source.getUploadType());
        target.setUploadId(source.getUploadId());
        target.setUploadStatus(source.getUploadStatus());
        target.setImageProcess(source.getImageProcess());
        target.setSort(source.getSort());
        target.setCreateTime(source.getCreateTime());
        target.setCreateBy(source.getCreateBy());
        target.setUpdateTime(source.getUpdateTime());
        target.setUpdateBy(source.getUpdateBy());
        target.setRemark(source.getRemark());
        target.setVersion(source.getVersion());
        return target;
    }

    public static MdmFileosRecord copyIfNotNull(MdmFileosRecord source, MdmFileosRecord target) {
        if (target == null) { target = new MdmFileosRecord(); }
        if (source == null) { return target; }
        if (source.getId() != null) { target.setId(source.getId()); }
        if (source.getTenantCode() != null) { target.setTenantCode(source.getTenantCode()); }
        if (source.getFileId() != null) { target.setFileId(source.getFileId()); }
        if (source.getFileName() != null) { target.setFileName(source.getFileName()); }
        if (source.getFileType() != null) { target.setFileType(source.getFileType()); }
        if (source.getFileSize() != null) { target.setFileSize(source.getFileSize()); }
        if (source.getFileHash() != null) { target.setFileHash(source.getFileHash()); }
        if (source.getContentType() != null) { target.setContentType(source.getContentType()); }
        if (source.getCategory() != null) { target.setCategory(source.getCategory()); }
        if (source.getDirPath() != null) { target.setDirPath(source.getDirPath()); }
        if (source.getIsPublic() != null) { target.setIsPublic(source.getIsPublic()); }
        if (source.getOssSp() != null) { target.setOssSp(source.getOssSp()); }
        if (source.getBucketName() != null) { target.setBucketName(source.getBucketName()); }
        if (source.getUploadType() != null) { target.setUploadType(source.getUploadType()); }
        if (source.getUploadId() != null) { target.setUploadId(source.getUploadId()); }
        if (source.getUploadStatus() != null) { target.setUploadStatus(source.getUploadStatus()); }
        if (source.getImageProcess() != null) { target.setImageProcess(source.getImageProcess()); }
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
