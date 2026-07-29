package com.wkclz.micro.flowable.bean.req;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "流程设计更新请求")
public class DesignUpdateReq {
    @NotNull(message = "id 不能为空")
    @Schema(description = "设计 ID")
    private Long id;
    @Schema(description = "BPMN XML 内容")
    private String xmlContent;
    @Schema(description = "流程名称")
    private String designName;
    @Schema(description = "流程分类")
    private String category;
    @Schema(description = "关联表单 key")
    private String formKey;
    @Schema(description = "乐观锁版本")
    private Integer version;
}
