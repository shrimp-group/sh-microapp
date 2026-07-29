package com.wkclz.micro.flowable.bean.req;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "流程设计上传请求")
public class DesignUploadReq {
    @NotBlank(message = "流程名称不能为空")
    @Schema(description = "流程名称")
    private String designName;
    @Schema(description = "流程分类")
    private String category;
    @NotBlank(message = "XML 内容不能为空")
    @Schema(description = "BPMN XML 内容")
    private String xmlContent;
    @Schema(description = "关联表单 key")
    private String formKey;
}
