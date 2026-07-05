package com.wkclz.micro.form.bean.resp;

import com.wkclz.web.bean.EntityResp;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "表单校验规则验证器响应")
public class MdmFormRuleValidatorResp extends EntityResp {

    @Schema(description = "表单校验规则编码")
    private String formRuleCode;

    @Schema(description = "字段编码")
    private String fieldCode;

    @Schema(description = "字段名称")
    private String fieldName;

    @Schema(description = "验证器类型")
    private String validatorType;

    @Schema(description = "验证匹配器")
    private String validatorPattern;

    @Schema(description = "验证函数")
    private String validatorFunction;

    @Schema(description = "模板编码")
    private String templateCode;

    @Schema(description = "验证消息模板")
    private String msgTemplate;
}
