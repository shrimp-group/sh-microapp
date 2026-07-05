package com.wkclz.micro.report.bean.req;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.io.Serializable;

@Data
@Schema(description = "报表参数创建请求")
public class ReportDefinitionParamCreateReq implements Serializable {

    @NotBlank(message = "报表编码不能为空")
    @Schema(description = "报表编码", requiredMode = Schema.RequiredMode.REQUIRED)
    private String reportCode;

    @NotBlank(message = "字段编码不能为空")
    @Schema(description = "字段编码", requiredMode = Schema.RequiredMode.REQUIRED)
    private String fieldCode;

    @NotBlank(message = "字段名称不能为空")
    @Schema(description = "字段名称", requiredMode = Schema.RequiredMode.REQUIRED)
    private String fieldName;

    @Schema(description = "字段类型")
    private String fieldType;

    @Schema(description = "表单类型")
    private String fieldFormType;

    @Schema(description = "输入提示")
    private String placeholder;

    @Schema(description = "是否必填")
    private Integer required;

    @Schema(description = "校验JS脚本")
    private String validateScript;

    @Schema(description = "字典类型")
    private String dictType;

    @Schema(description = "列表宽度")
    private Integer width;

    @Schema(description = "排序")
    private Integer sort;

    @Schema(description = "备注")
    private String remark;
}
