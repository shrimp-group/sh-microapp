package com.wkclz.micro.flowable.bean.resp;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "设计上传响应")
public class DesignUploadResp {
    @Schema(description = "设计 ID")
    private Long designId;
    @Schema(description = "设计编码")
    private String designCode;
    @Schema(description = "设计版本")
    private Integer version;
}
