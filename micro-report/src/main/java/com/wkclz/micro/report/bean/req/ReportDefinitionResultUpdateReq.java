package com.wkclz.micro.report.bean.req;

import com.wkclz.web.bean.UpdateReq;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "报表结果字段修改请求")
public class ReportDefinitionResultUpdateReq extends UpdateReq {

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

    @Schema(description = "排序")
    private Integer sort;

    @Schema(description = "备注")
    private String remark;
}
