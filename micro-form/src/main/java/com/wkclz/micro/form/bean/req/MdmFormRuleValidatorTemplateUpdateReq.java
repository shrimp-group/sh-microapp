package com.wkclz.micro.form.bean.req;

import com.wkclz.web.bean.UpdateReq;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "表单校验规则模板修改请求")
public class MdmFormRuleValidatorTemplateUpdateReq extends UpdateReq {

    @Schema(description = "模板编码")
    private String templateCode;

    @NotBlank(message = "模板名称不能为空")
    @Schema(description = "模板名称", requiredMode = Schema.RequiredMode.REQUIRED)
    private String templateName;

    @Schema(description = "表单校验正则")
    private String validatorPattern;

    @Schema(description = "表单验证函数")
    private String validatorFunction;

    @Schema(description = "排序")
    private Integer sort;

    @Schema(description = "备注")
    private String remark;
}
