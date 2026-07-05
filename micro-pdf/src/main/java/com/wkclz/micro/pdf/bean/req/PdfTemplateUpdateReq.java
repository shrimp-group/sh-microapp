package com.wkclz.micro.pdf.bean.req;

import com.wkclz.web.bean.UpdateReq;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "PDF模板修改请求")
public class PdfTemplateUpdateReq extends UpdateReq {

    @NotBlank(message = "templateName 不能为空")
    @Schema(description = "模板名称")
    private String templateName;

    @NotBlank(message = "templateContext 不能为空")
    @Schema(description = "模板内容")
    private String templateContext;

    @Schema(description = "模拟数据")
    private String mockData;

    @Schema(description = "排序")
    private Integer sort;

    @Schema(description = "备注")
    private String remark;
}
