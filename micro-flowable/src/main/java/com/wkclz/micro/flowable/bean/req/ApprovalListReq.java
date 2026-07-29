package com.wkclz.micro.flowable.bean.req;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "审批意见列表查询")
public class ApprovalListReq {
    @Schema(description = "流程实例 ID")
    private String procInsId;
    @Schema(description = "申请单 ID")
    private Long applyId;
}
