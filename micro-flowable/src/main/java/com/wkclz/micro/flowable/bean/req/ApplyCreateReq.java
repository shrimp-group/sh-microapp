package com.wkclz.micro.flowable.bean.req;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.Map;

@Data
@Schema(description = "流程申请创建请求")
public class ApplyCreateReq {
    @NotBlank(message = "设计编码不能为空")
    @Schema(description = "关联流程设计编码")
    private String designCode;
    @Schema(description = "业务类型")
    private String businessType;
    @Schema(description = "申请内容摘要")
    private String businessSummary;
    @Schema(description = "业务表单数据 JSON")
    private String businessData;
    @Schema(description = "流程变量")
    private Map<String, Object> variables;
}
