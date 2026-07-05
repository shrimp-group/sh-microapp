package com.wkclz.micro.fileos.bean.req;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

@Data
@Schema(description = "简单上传请求")
public class UploadSimpleReq implements Serializable {

    @Schema(description = "业务分类")
    private String category;

    @Schema(description = "Bucket名称")
    private String bucketName;

    @Schema(description = "自定义文件名")
    private String fileName;

    @Schema(description = "是否公共读")
    private Boolean isPublic;

    @Schema(description = "图片处理参数（JSON）")
    private String imageProcess;
}
