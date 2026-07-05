package com.wkclz.micro.fileos.bean.resp;

import com.wkclz.web.bean.EntityResp;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "文件记录响应")
public class RecordResp extends EntityResp implements Serializable {

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

    @Schema(description = "预览签名URL")
    private String previewUrl;
}
