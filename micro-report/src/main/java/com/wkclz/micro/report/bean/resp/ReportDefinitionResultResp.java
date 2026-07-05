package com.wkclz.micro.report.bean.resp;

import com.wkclz.web.bean.EntityResp;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "报表结果字段响应")
public class ReportDefinitionResultResp extends EntityResp {

    @Schema(description = "报表编码")
    private String reportCode;

    @Schema(description = "字段编码")
    private String fieldCode;

    @Schema(description = "字段名称")
    private String fieldName;

    @Schema(description = "字段类型")
    private String fieldType;

    @Schema(description = "展示类型")
    private String fieldFormType;

    @Schema(description = "列宽")
    private Integer width;

}
