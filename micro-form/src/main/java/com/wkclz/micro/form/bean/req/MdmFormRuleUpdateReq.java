package com.wkclz.micro.form.bean.req;

import com.wkclz.web.bean.UpdateReq;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "表单校验规则修改请求")
public class MdmFormRuleUpdateReq extends UpdateReq {

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
