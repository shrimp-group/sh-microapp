package com.wkclz.micro.report.bean.req;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.io.Serializable;

@Data
@Schema(description = "报表结果字段创建请求")
public class ReportDefinitionResultCreateReq implements Serializable {

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

    @Schema(description = "展示类型")
    private String fieldFormType;

    @Schema(description = "列宽")
    private Integer width;

    @Schema(description = "排序")
    private Integer sort;

    @Schema(description = "备注")
    private String remark;
}
