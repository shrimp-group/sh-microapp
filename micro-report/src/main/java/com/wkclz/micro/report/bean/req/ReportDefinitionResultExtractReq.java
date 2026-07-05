package com.wkclz.micro.report.bean.req;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.io.Serializable;

@Data
@Schema(description = "报表结果字段自动提取请求")
public class ReportDefinitionResultExtractReq implements Serializable {

    @Schema(description = "报表编码")
    private String reportCode;

    @NotBlank(message = "SQL脚本不能为空")
    @Schema(description = "SQL查询脚本", requiredMode = Schema.RequiredMode.REQUIRED)
    private String reportScript;

    @Schema(description = "自动驼峰转换")
    private Integer reportScriptAutocamel;
}
