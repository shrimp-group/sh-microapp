package com.wkclz.micro.fileos.bean.req;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.io.Serializable;

@Data
@Schema(description = "分片上传中止请求")
public class MultipartAbortReq implements Serializable {

    @NotBlank(message = "分片上传ID不能为空")
    @Schema(description = "分片上传ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private String uploadId;

    @NotBlank(message = "文件存储路径不能为空")
    @Schema(description = "文件存储路径", requiredMode = Schema.RequiredMode.REQUIRED)
    private String fileId;

    @Schema(description = "Bucket名称")
    private String bucketName;

    @Schema(description = "OSS服务商")
    private String ossSp;
}
