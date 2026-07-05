package com.wkclz.micro.report.bean.req;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.io.Serializable;

@Data
@Schema(description = "报表定义创建请求")
public class ReportDefinitionCreateReq implements Serializable {

    @Schema(description = "报表编码【为空则自动生成】")
    private String reportCode;

    @NotBlank(message = "报表名称不能为空")
    @Schema(description = "报表名称", requiredMode = Schema.RequiredMode.REQUIRED)
    private String reportName;

    @Schema(description = "返回值类型：OBJECT/LIST/PAGE")
    private String resultType;

    @Schema(description = "启用状态")
    private Integer enableFlag;

    @Schema(description = "SQL查询脚本")
    private String reportScript;

    @Schema(description = "Count脚本开关")
    private Integer reportScriptCountSwitch;

    @Schema(description = "自定义COUNT SQL脚本")
    private String reportScriptCount;

    @Schema(description = "自动驼峰转换")
    private Integer reportScriptAutocamel;

    @Schema(description = "排序")
    private Integer sort;

    @Schema(description = "备注")
    private String remark;
}
