package com.wkclz.micro.form.bean.req;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.io.Serializable;

@Data
@Schema(description = "表单校验规则创建请求")
public class MdmFormRuleCreateReq implements Serializable {

    @Schema(description = "表单校验规则编码")
    private String formRuleCode;

    @NotBlank(message = "表单校验规则名称不能为空")
    @Schema(description = "表单校验规则名称", requiredMode = Schema.RequiredMode.REQUIRED)
    private String formRuleName;

    @NotBlank(message = "API方法不能为空")
    @Schema(description = "API方法", requiredMode = Schema.RequiredMode.REQUIRED)
    private String apiMethod;

    @NotBlank(message = "API路径不能为空")
    @Schema(description = "API路径", requiredMode = Schema.RequiredMode.REQUIRED)
    private String apiUri;

    @Schema(description = "排序")
    private Integer sort;

    @Schema(description = "备注")
    private String remark;
}
