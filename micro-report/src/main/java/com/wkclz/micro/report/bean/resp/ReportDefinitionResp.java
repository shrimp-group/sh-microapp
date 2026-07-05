package com.wkclz.micro.report.bean.resp;

import com.wkclz.web.bean.EntityResp;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "报表定义响应")
public class ReportDefinitionResp extends EntityResp {

    @Schema(description = "报表编码")
    private String reportCode;

    @Schema(description = "报表名称")
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
}
