package com.wkclz.micro.flowable.bean.resp;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "申请详情响应")
public class ApplyInfoResp extends ApplyPageResp {
    @Schema(description = "业务表单数据 JSON")
    private String businessData;
    @Schema(description = "流程定义 ID")
    private String procDefId;
}
