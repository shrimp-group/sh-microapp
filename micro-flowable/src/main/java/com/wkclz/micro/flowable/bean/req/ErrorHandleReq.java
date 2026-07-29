package com.wkclz.micro.flowable.bean.req;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "异常处理请求")
public class ErrorHandleReq {
    @NotNull(message = "id 不能为空")
    @Schema(description = "异常日志 ID")
    private Long id;
    @Schema(description = "处理状态：RESOLVED/IGNORED")
    private String handleStatus;
    @Schema(description = "备注")
    private String remark;
}
