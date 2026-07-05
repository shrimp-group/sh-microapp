package com.wkclz.micro.form.bean.req;

import com.wkclz.web.bean.PageReq;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "表单校验规则分页查询请求")
public class MdmFormRulePageReq extends PageReq {

    @Schema(description = "表单校验规则编码【支持模糊查询】")
    private String formRuleCode;

    @Schema(description = "表单校验规则名称【支持模糊查询】")
    private String formRuleName;
}
