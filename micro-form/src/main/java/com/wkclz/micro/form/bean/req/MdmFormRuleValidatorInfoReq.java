package com.wkclz.micro.form.bean.req;

import com.wkclz.web.bean.IdReq;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "表单校验规则验证器详情查询请求")
public class MdmFormRuleValidatorInfoReq extends IdReq {
}
