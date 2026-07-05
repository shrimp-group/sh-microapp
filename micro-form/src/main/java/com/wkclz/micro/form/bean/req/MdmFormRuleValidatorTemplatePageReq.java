package com.wkclz.micro.form.bean.req;

import com.wkclz.web.bean.PageReq;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "表单校验规则模板分页查询请求")
public class MdmFormRuleValidatorTemplatePageReq extends PageReq {

    @Schema(description = "模板编码【支持模糊查询】")
    private String templateCode;

    @Schema(description = "模板名称【支持模糊查询】")
    private String templateName;

    @Schema(description = "表单校验正则")
    private String validatorPattern;
}
