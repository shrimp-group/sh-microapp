package com.wkclz.micro.pdf.bean.req;

import com.wkclz.web.bean.PageReq;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "PDF模板分页查询请求")
public class PdfTemplatePageReq extends PageReq {

    @Schema(description = "模板编码")
    private String templateCode;

    @Schema(description = "模板名称")
    private String templateName;
}
