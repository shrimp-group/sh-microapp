package com.wkclz.micro.report.bean.resp;

import com.wkclz.web.bean.EntityResp;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "报表定义历史分页响应")
public class ReportDefinitionHisPageResp extends EntityResp {

    @Schema(description = "原数据ID")
    private Long dataId;

    @Schema(description = "报表编码")
    private String reportCode;

    @Schema(description = "报表名称")
    private String reportName;

    @Schema(description = "返回值类型：OBJECT/LIST/PAGE")
    private String resultType;

    @Schema(description = "启用状态")
    private Integer enableFlag;
}
