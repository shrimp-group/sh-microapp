package com.wkclz.micro.pdf.bean.resp;

import com.wkclz.web.bean.EntityResp;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "PDF模板分页响应")
public class PdfTemplatePageResp extends EntityResp {

    @Schema(description = "模板编码")
    private String templateCode;

    @Schema(description = "模板名称")
    private String templateName;
}
