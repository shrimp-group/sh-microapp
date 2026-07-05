package com.wkclz.micro.report.bean.req;

import com.wkclz.web.bean.IdReq;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "报表定义历史详情查询请求")
public class ReportDefinitionHisInfoReq extends IdReq {
}
