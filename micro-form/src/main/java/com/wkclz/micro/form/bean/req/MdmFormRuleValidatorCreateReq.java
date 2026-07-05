package com.wkclz.micro.form.bean.req;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.io.Serializable;

@Data
@Schema(description = "表单校验规则验证器创建请求")
public class MdmFormRuleValidatorCreateReq implements Serializable {

    @NotBlank(message = "表单校验规则编码不能为空")
    @Schema(description = "表单校验规则编码", requiredMode = Schema.RequiredMode.REQUIRED)
    private String formRuleCode;

    @NotBlank(message = "字段编码不能为空")
    @Schema(description = "字段编码", requiredMode = Schema.RequiredMode.REQUIRED)
    private String fieldCode;

    @NotBlank(message = "字段名称不能为空")
    @Schema(description = "字段名称", requiredMode = Schema.RequiredMode.REQUIRED)
    private String fieldName;

    @NotBlank(message = "验证器类型不能为空")
    @Schema(description = "验证器类型", requiredMode = Schema.RequiredMode.REQUIRED)
    private String validatorType;

    @Schema(description = "验证匹配器")
    private String validatorPattern;

    @Schema(description = "验证函数")
    private String validatorFunction;

    @Schema(description = "模板编码")
    private String templateCode;

    @Schema(description = "验证消息模板")
    private String msgTemplate;

    @Schema(description = "排序")
    private Integer sort;
}
