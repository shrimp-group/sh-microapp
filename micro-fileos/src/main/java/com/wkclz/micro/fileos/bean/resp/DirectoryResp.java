package com.wkclz.micro.fileos.bean.resp;

import com.wkclz.web.bean.EntityResp;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "目录响应")
public class DirectoryResp extends EntityResp implements Serializable {

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
}
