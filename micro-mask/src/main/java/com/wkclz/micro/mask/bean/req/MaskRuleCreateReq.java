package com.wkclz.micro.mask.bean.req;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.io.Serializable;

@Data
@Schema(description = "脱敏规则创建请求")
public class MaskRuleCreateReq implements Serializable {

    @Schema(description = "脱敏规则编码")
    private String maskRuleCode;

    @NotBlank(message = "脱敏规则名称不能为空")
    @Schema(description = "脱敏规则名称", requiredMode = Schema.RequiredMode.REQUIRED)
    private String maskRuleName;

    @NotBlank(message = "请求方法不能为空")
    @Schema(description = "请求方法", requiredMode = Schema.RequiredMode.REQUIRED)
    private String requestMethod;

    @NotBlank(message = "请求路径不能为空")
    @Schema(description = "请求路径,支持AntPathMatcher", requiredMode = Schema.RequiredMode.REQUIRED)
    private String requestUri;

    @NotBlank(message = "脱敏数据路径不能为空")
    @Schema(description = "脱敏数据路径", requiredMode = Schema.RequiredMode.REQUIRED)
    private String maskJsonPath;

    @Schema(description = "脱敏正则")
    private String maskRuleRegular;

    @Schema(description = "脱敏函数")
    private String maskRuleScript;

    @Schema(description = "可用状态")
    private Integer enableFlag;

    @Schema(description = "示例值")
    private String mockValue;

    @Schema(description = "排序")
    private Integer sort;

    @Schema(description = "备注")
    private String remark;
}
