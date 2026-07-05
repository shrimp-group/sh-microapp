package com.wkclz.micro.form.bean.req;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.io.Serializable;

@Data
@Schema(description = "通用表单验证规则请求")
public class CommonFormRuleReq implements Serializable {

    @NotBlank(message = "API方法不能为空")
    @Schema(description = "API方法", requiredMode = Schema.RequiredMode.REQUIRED)
    private String apiMethod;

    @NotBlank(message = "API路径不能为空")
    @Schema(description = "API路径", requiredMode = Schema.RequiredMode.REQUIRED)
    private String apiUri;
}
