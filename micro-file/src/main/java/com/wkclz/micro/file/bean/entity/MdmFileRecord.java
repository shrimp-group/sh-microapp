package com.wkclz.micro.file.bean.entity;

import com.wkclz.core.annotation.FieldDesc;
import com.wkclz.core.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;




/**
 * Description Create by sh-generator
 * @author shrimp
 * @table mdm_file_record (附件) 重新生成代码会覆盖
 */

@Data
@EqualsAndHashCode(callSuper = false)
public class MdmFileRecord extends BaseEntity {

    /**
     * 租户编码
     */
    @FieldDesc("租户编码")
    private String tenantCode;

    /**
     * 业务类型
     */
    @FieldDesc("业务类型")
    private String businessType;

    /**
     * 文件大小
     */
    @FieldDesc("文件大小")
    private Long fileSize;

    /**
     * 文件名
     */
    @FieldDesc("文件名")
    private String fileName;

    /**
     * 文件类型
     */
    @FieldDesc("文件类型")
    private String fileType;

    /**
     * OSS服务商
     */
    @FieldDesc("OSS服务商")
    private String ossSp;

    /**
     * Bucket
     */
    @FieldDesc("Bucket")
    private String bucket;

    /**
     * 文件ID/路径
     */
    @FieldDesc("文件ID/路径")
    private String fileId;


    public static MdmFileRecord copy(MdmFileRecord source, MdmFileRecord target) {
        if (target == null ) { target = new MdmFileRecord();}
        if (source == null) { return target; }
        target.setId(source.getId());
        target.setTenantCode(source.getTenantCode());
        target.setBusinessType(source.getBusinessType());
        target.setFileSize(source.getFileSize());
        target.setFileName(source.getFileName());
        target.setFileType(source.getFileType());
        target.setOssSp(source.getOssSp());
        target.setBucket(source.getBucket());
        target.setFileId(source.getFileId());
        target.setSort(source.getSort());
        target.setCreateTime(source.getCreateTime());
        target.setCreateBy(source.getCreateBy());
        target.setUpdateTime(source.getUpdateTime());
        target.setUpdateBy(source.getUpdateBy());
        target.setRemark(source.getRemark());
        target.setVersion(source.getVersion());
        return target;
    }

    public static MdmFileRecord copyIfNotNull(MdmFileRecord source, MdmFileRecord target) {
        if (target == null ) { target = new MdmFileRecord();}
        if (source == null) { return target; }
        if (source.getId() != null) { target.setId(source.getId()); }
        if (source.getTenantCode() != null) { target.setTenantCode(source.getTenantCode()); }
        if (source.getBusinessType() != null) { target.setBusinessType(source.getBusinessType()); }
        if (source.getFileSize() != null) { target.setFileSize(source.getFileSize()); }
        if (source.getFileName() != null) { target.setFileName(source.getFileName()); }
        if (source.getFileType() != null) { target.setFileType(source.getFileType()); }
        if (source.getOssSp() != null) { target.setOssSp(source.getOssSp()); }
        if (source.getBucket() != null) { target.setBucket(source.getBucket()); }
        if (source.getFileId() != null) { target.setFileId(source.getFileId()); }
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

