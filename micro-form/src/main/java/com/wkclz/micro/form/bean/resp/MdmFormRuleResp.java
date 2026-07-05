package com.wkclz.micro.form.bean.resp;

import com.wkclz.web.bean.EntityResp;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "表单校验规则响应")
public class MdmFormRuleResp extends EntityResp {

    @Schema(description = "表单校验规则编码")
    private String formRuleCode;

    @Schema(description = "表单校验规则名称")
    private String formRuleName;

    @Schema(description = "API方法")
    private String apiMethod;

    @Schema(description = "API路径")
    private String apiUri;

    @Schema(description = "规则数量")
    private Integer itemCount;
}
