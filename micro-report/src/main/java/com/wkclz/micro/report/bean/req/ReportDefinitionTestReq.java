package com.wkclz.micro.report.bean.req;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.io.Serializable;
import java.util.Map;

@Data
@Schema(description = "SQL测试请求")
public class ReportDefinitionTestReq implements Serializable {

    @Schema(description = "报表编码")
    private String reportCode;

    @Schema(description = "返回值类型：OBJECT/LIST/PAGE")
    private String resultType;

    @NotBlank(message = "SQL脚本不能为空")
    @Schema(description = "SQL查询脚本", requiredMode = Schema.RequiredMode.REQUIRED)
    private String reportScript;

    @Schema(description = "自定义COUNT SQL脚本")
    private String reportScriptCount;

    @Schema(description = "Count脚本开关")
    private Integer reportScriptCountSwitch;

    @Schema(description = "自动驼峰转换")
    private Integer reportScriptAutocamel;

    @Schema(description = "当前页码")
    private Integer current;

    @Schema(description = "每页条数")
    private Integer size;

    @Schema(description = "查询参数")
    private Map<String, Object> params;
}
