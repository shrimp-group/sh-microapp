package com.wkclz.micro.report.bean.req;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.io.Serializable;
import java.util.Map;

@Data
@Schema(description = "报表详情查询请求")
public class ReportExecInfoReq implements Serializable {

    @NotBlank(message = "报表编码不能为空")
    @Schema(description = "报表编码", requiredMode = Schema.RequiredMode.REQUIRED)
    private String reportCode;


    @Schema(description = "查询参数")
    private Map<String, Object> params;

}
