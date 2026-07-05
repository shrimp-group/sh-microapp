package com.wkclz.micro.mask.bean.resp;

import com.wkclz.web.bean.EntityResp;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "脱敏规则分页响应")
public class MaskRulePageResp extends EntityResp {

    @Schema(description = "脱敏规则编码")
    private String maskRuleCode;

    @Schema(description = "脱敏规则名称")
    private String maskRuleName;

    @Schema(description = "请求方法")
    private String requestMethod;

    @Schema(description = "请求路径")
    private String requestUri;

    @Schema(description = "脱敏数据路径")
    private String maskJsonPath;

    @Schema(description = "脱敏正则")
    private String maskRuleRegular;

    @Schema(description = "可用状态")
    private Integer enableFlag;
}
