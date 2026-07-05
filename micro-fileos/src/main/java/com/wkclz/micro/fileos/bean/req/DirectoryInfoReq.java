package com.wkclz.micro.fileos.bean.req;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.io.Serializable;

@Data
@Schema(description = "目录详情查询请求")
public class DirectoryInfoReq implements Serializable {

    @NotBlank(message = "目录路径不能为空")
    @Schema(description = "目录路径", requiredMode = Schema.RequiredMode.REQUIRED)
    private String dirPath;

    @Schema(description = "所属Bucket")
    private String bucketName;
}
