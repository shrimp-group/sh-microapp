package com.wkclz.micro.pdf.bean.req;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
@Schema(description = "PDF模板Mock预览请求")
public class PdfTemplateMockReq {

    @NotBlank(message = "templateContext 不能为空")
    @Schema(description = "模板内容")
    private String templateContext;

    @Schema(description = "模拟数据")
    private String mockData;
}
