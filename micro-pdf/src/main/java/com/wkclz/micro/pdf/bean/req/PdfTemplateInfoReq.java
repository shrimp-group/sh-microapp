package com.wkclz.micro.pdf.bean.req;

import com.wkclz.web.bean.IdReq;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "PDF模板详情查询请求")
public class PdfTemplateInfoReq extends IdReq {
}
