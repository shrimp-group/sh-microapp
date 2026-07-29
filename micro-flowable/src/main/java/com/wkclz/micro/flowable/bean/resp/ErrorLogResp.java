package com.wkclz.micro.flowable.bean.resp;

import com.wkclz.web.bean.EntityResp;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "异常日志响应")
public class ErrorLogResp extends EntityResp {
    @Schema(description = "异常类型")
    private String errorType;
    @Schema(description = "流程实例 ID")
    private String procInsId;
    @Schema(description = "任务 ID")
    private String taskId;
    @Schema(description = "申请单 ID")
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
    @Schema(description = "处理状态")
    private String handleStatus;
}
