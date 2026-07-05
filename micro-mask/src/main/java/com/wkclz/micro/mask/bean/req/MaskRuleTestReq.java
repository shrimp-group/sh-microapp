package com.wkclz.micro.mask.bean.req;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.io.Serializable;

@Data
@Schema(description = "脱敏规则测试请求")
public class MaskRuleTestReq implements Serializable {

    @NotBlank(message = "示例值不能为空")
    @Schema(description = "示例值", requiredMode = Schema.RequiredMode.REQUIRED)
    private String mockValue;

    @Schema(description = "脱敏正则")
    private String maskRuleRegular;

    @Schema(description = "脱敏函数")
    private String maskRuleScript;
}
