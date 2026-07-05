package com.wkclz.micro.report.bean.req;

import com.wkclz.core.annotation.FieldDesc;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.io.Serializable;

@Data
@Schema(description = "报表结果字段列表查询请求")
public class ReportDefinitionResultListReq implements Serializable {

    @NotBlank(message = "报表编码不能为空")
    @Schema(description = "报表编码", requiredMode = Schema.RequiredMode.REQUIRED)
    private String reportCode;

    @FieldDesc("字段编码")
    private String fieldCode;

    @FieldDesc("字段名称")
    private String fieldName;
}
