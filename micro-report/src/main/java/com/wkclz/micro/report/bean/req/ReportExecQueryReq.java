package com.wkclz.micro.report.bean.req;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.io.Serializable;
import java.util.Map;

@Data
@Schema(description = "报表执行查询请求")
public class ReportExecQueryReq implements Serializable {

    @NotBlank(message = "报表编码不能为空")
    @Schema(description = "报表编码", requiredMode = Schema.RequiredMode.REQUIRED)
    private String reportCode;

    @Schema(description = "当前页码")
    private Integer current;

    @Schema(description = "每页条数")
    private Integer size;

    @Schema(description = "查询参数")
    private Map<String, Object> params;
}
