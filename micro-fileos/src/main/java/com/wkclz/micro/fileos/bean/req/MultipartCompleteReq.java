package com.wkclz.micro.fileos.bean.req;

import com.wkclz.micro.fileos.bean.dto.CompletedPartInfo;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
@Schema(description = "分片上传完成请求")
public class MultipartCompleteReq implements Serializable {

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

    @Schema(description = "原始文件名")
    private String fileName;

    @Schema(description = "文件大小")
    private Long fileSize;

    @Schema(description = "业务分类")
    private String category;

    @Schema(description = "是否公共读")
    private Boolean isPublic;

    @Schema(description = "已完成分片信息")
    private List<CompletedPartInfo> parts;
}
