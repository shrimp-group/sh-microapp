package com.wkclz.micro.fileos.bean.req;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.io.Serializable;

@Data
@Schema(description = "预签名上传完成确认请求")
public class PresignCompleteReq implements Serializable {

    @NotBlank(message = "文件存储路径不能为空")
    @Schema(description = "文件存储路径", requiredMode = Schema.RequiredMode.REQUIRED)
    private String fileId;

    @Schema(description = "OSS服务商")
    private String ossSp;

    @Schema(description = "Bucket名称")
    private String bucketName;

    @Schema(description = "原始文件名")
    private String fileName;

    @Schema(description = "文件大小")
    private Long fileSize;

    @Schema(description = "业务分类")
    private String category;

    @Schema(description = "是否公共读")
    private Boolean isPublic;

    @Schema(description = "图片处理参数（JSON）")
    private String imageProcess;
}
