package com.wkclz.micro.flowable.bean.resp;

import com.wkclz.web.bean.EntityResp;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "申请分页响应")
public class ApplyPageResp extends EntityResp {
    @Schema(description = "申请单号")
    private String applyCode;
    @Schema(description = "设计编码")
    private String designCode;
    @Schema(description = "流程实例 ID")
    private String procInsId;
    @Schema(description = "业务类型")
    private String businessType;
    @Schema(description = "申请内容摘要")
    private String businessSummary;
    @Schema(description = "发起人")
    private String startUserId;
    @Schema(description = "状态")
    private String status;
}
