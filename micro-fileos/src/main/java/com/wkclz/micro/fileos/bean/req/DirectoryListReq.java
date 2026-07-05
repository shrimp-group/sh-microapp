package com.wkclz.micro.fileos.bean.req;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.io.Serializable;

@Data
@Schema(description = "目录列表查询请求")
public class DirectoryListReq implements Serializable {

    @NotBlank(message = "父目录路径不能为空")
    @Schema(description = "父目录路径", requiredMode = Schema.RequiredMode.REQUIRED)
    private String parentPath;

    @Schema(description = "所属Bucket")
    private String bucketName;
}
