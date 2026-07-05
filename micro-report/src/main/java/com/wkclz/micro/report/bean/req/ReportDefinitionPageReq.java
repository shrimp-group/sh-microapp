package com.wkclz.micro.report.bean.req;

import com.wkclz.web.bean.PageReq;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "报表定义分页查询请求")
public class ReportDefinitionPageReq extends PageReq {

    @Schema(description = "报表编码【支持模糊查询】")
    private String reportCode;

    @Schema(description = "报表名称【支持模糊查询】")
    private String reportName;

    @Schema(description = "返回值类型：OBJECT/LIST/PAGE")
    private String resultType;

    @Schema(description = "启用状态")
    private Integer enableFlag;
}
