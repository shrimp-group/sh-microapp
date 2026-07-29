package com.wkclz.micro.flowable.bean.req;

import com.wkclz.web.bean.PageReq;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "异常日志分页查询")
public class ErrorPageReq extends PageReq {
    @Schema(description = "异常类型")
    private String errorType;
    @Schema(description = "处理状态")
    private String handleStatus;
    @Schema(description = "流程实例 ID")
    private String procInsId;
}
