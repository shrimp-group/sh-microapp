package com.wkclz.micro.form.bean.req;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.io.Serializable;

@Data
@Schema(description = "表单校验规则模板创建请求")
public class MdmFormRuleValidatorTemplateCreateReq implements Serializable {

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
