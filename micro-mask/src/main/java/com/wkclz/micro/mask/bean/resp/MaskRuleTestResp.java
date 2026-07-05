package com.wkclz.micro.mask.bean.resp;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

@Data
@Schema(description = "脱敏规则测试响应")
public class MaskRuleTestResp implements Serializable {

    @Schema(description = "示例值")
    private String mockValue;

    @Schema(description = "脱敏后的值")
    private String maskValue;

    @Schema(description = "脱敏方式说明")
    private String maskType;
}
