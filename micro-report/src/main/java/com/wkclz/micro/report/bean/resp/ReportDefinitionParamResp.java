package com.wkclz.micro.report.bean.resp;

import com.wkclz.web.bean.EntityResp;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "报表参数响应")
public class ReportDefinitionParamResp extends EntityResp {

    @Schema(description = "报表编码")
    private String reportCode;

    @Schema(description = "字段编码")
    private String fieldCode;

    @Schema(description = "字段名称")
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

}
