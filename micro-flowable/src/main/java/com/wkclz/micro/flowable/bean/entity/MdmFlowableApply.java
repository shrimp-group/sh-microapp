package com.wkclz.micro.flowable.bean.entity;

import com.wkclz.core.base.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
@Schema(description = "流程申请单")
public class MdmFlowableApply extends BaseEntity {

    @Schema(description = "申请单号")
    private String applyCode;

    @Schema(description = "关联流程设计编码")
    private String designCode;

    @Schema(description = "flowable 流程实例 ID")
    private String procInsId;

    @Schema(description = "流程定义 ID")
    private String procDefId;

    @Schema(description = "业务类型")
    private String businessType;

    @Schema(description = "申请内容摘要")
    private String businessSummary;

    @Schema(description = "业务表单数据 JSON")
    private String businessData;

    @Schema(description = "发起人用户 ID")
    private String startUserId;

    @Schema(description = "状态：RUNNING/APPROVED/REJECTED/TERMINATED/WITHDRAWN")
    private String status;

    @Schema(description = "租户编码")
    private String tenantCode;
}
