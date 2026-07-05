package com.wkclz.micro.form.bean.req;

import com.wkclz.web.bean.UpdateReq;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "表单校验规则验证器修改请求")
public class MdmFormRuleValidatorUpdateReq extends UpdateReq {

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
