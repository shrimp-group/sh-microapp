package com.wkclz.micro.flowable.bean.entity;

import com.wkclz.core.base.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
@Schema(description = "流程设计")
public class FlowableProcessDesign extends BaseEntity {

    @Schema(description = "设计编码")
    private String designCode;

    @Schema(description = "流程名称")
    private String designName;

    @Schema(description = "流程分类")
    private String category;

    @Schema(description = "BPMN XML 内容")
    private String xmlContent;

    @Schema(description = "关联表单 key")
    private String formKey;

    @Schema(description = "设计版本")
    private Integer designVersion;

    @Schema(description = "状态：DRAFT/DEPLOYED/DISABLED")
    private String status;

    @Schema(description = "最近部署 ID")
    private String deployId;

    @Schema(description = "最近流程定义 ID")
    private String procDefId;

    @Schema(description = "租户编码")
    private String tenantCode;
}
