package com.wkclz.micro.fileos.bean.resp;

import com.wkclz.micro.fileos.bean.dto.PresignedPartInfo;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
@Schema(description = "分片上传初始化响应")
public class MultipartUploadInitResp implements Serializable {

    @Schema(description = "分片上传ID")
    private String uploadId;

    @Schema(description = "文件存储路径")
    private String fileId;

    @Schema(description = "OSS服务商")
    private String ossSp;

    @Schema(description = "Bucket名称")
    private String bucketName;

    @Schema(description = "MIME类型")
    private String contentType;

    @Schema(description = "过期时间（分钟）")
    private Integer expireMinutes;

    @Schema(description = "分片预签名信息")
    private List<PresignedPartInfo> parts;
}
