package com.wkclz.micro.form.bean.resp;

import com.wkclz.web.bean.EntityResp;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "表单校验规则模板响应")
public class MdmFormRuleValidatorTemplateResp extends EntityResp {

    @Schema(description = "模板编码")
    private String templateCode;

    @Schema(description = "模板名称")
    private String templateName;

    @Schema(description = "表单校验正则")
    private String validatorPattern;

    @Schema(description = "表单验证函数")
    private String validatorFunction;
}
