package com.wkclz.micro.flowable.bean.resp;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "申请创建响应")
public class ApplyCreateResp {
    @Schema(description = "申请单号")
    private String applyCode;
    @Schema(description = "流程实例 ID")
    private String procInsId;
}
