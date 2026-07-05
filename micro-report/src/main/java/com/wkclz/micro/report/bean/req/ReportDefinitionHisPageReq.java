package com.wkclz.micro.report.bean.req;

import com.wkclz.web.bean.PageReq;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "报表定义历史分页查询请求")
public class ReportDefinitionHisPageReq extends PageReq {

    @Schema(description = "原数据ID")
    private Long dataId;

    @Schema(description = "报表编码【支持模糊查询】")
    private String reportCode;

    @Schema(description = "报表名称【支持模糊查询】")
    private String reportName;
}
