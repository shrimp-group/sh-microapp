package com.wkclz.micro.flowable.bean.entity;

import com.wkclz.core.base.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = false)
@Schema(description = "异常日志")
public class MdmFlowableErrorLog extends BaseEntity {

    @Schema(description = "异常类型")
    private String errorType;

    @Schema(description = "关联流程实例")
    private String procInsId;

    @Schema(description = "关联任务")
    private String taskId;

    @Schema(description = "关联申请单")
    private Long applyId;

    @Schema(description = "client 调用方法")
    private String clientMethod;

    @Schema(description = "请求参数 JSON")
    private String requestData;

    @Schema(description = "异常消息")
    private String errorMessage;

    @Schema(description = "异常堆栈")
    private String errorStack;

    @Schema(description = "发生时间")
    private LocalDateTime occurTime;

    @Schema(description = "处理状态：PENDING/RESOLVED/IGNORED")
    private String handleStatus;

    @Schema(description = "租户编码")
    private String tenantCode;
}
