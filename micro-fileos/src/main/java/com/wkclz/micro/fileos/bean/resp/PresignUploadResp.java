package com.wkclz.micro.fileos.bean.resp;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

@Data
@Schema(description = "预签名上传响应")
public class PresignUploadResp implements Serializable {

    @Schema(description = "文件存储路径")
    private String fileId;

    @Schema(description = "预签名URL")
    private String presignUrl;

    @Schema(description = "OSS服务商")
    private String ossSp;

    @Schema(description = "Bucket名称")
    private String bucketName;

    @Schema(description = "MIME类型")
    private String contentType;

    @Schema(description = "过期时间（分钟）")
    private Integer expireMinutes;
}
