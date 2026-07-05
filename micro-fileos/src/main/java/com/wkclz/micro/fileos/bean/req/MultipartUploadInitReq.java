package com.wkclz.micro.fileos.bean.req;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.io.Serializable;

@Data
@Schema(description = "分片上传初始化请求")
public class MultipartUploadInitReq implements Serializable {

    @NotBlank(message = "原始文件名不能为空")
    @Schema(description = "原始文件名", requiredMode = Schema.RequiredMode.REQUIRED)
    private String fileName;

    @Schema(description = "文件大小")
    private Long fileSize;

    @Schema(description = "MIME类型")
    private String contentType;

    @Schema(description = "业务分类")
    private String category;

    @Schema(description = "Bucket名称")
    private String bucketName;

    @Schema(description = "是否公共读")
    private Boolean isPublic;

    @Schema(description = "分片总数")
    private Integer partCount;

    @Schema(description = "过期时间（分钟）")
    private Integer expireMinutes;

    @Schema(description = "图片处理参数（JSON）")
    private String imageProcess;
}
