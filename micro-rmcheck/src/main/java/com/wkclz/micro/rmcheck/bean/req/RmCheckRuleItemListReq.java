package com.wkclz.micro.rmcheck.bean.req;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.io.Serializable;

@Data
@Schema(description = "删除检查规则检查项列表查询请求")
public class RmCheckRuleItemListReq implements Serializable {

    @NotBlank(message = "规则编码不能为空")
    @Schema(description = "规则编码", requiredMode = Schema.RequiredMode.REQUIRED)
    private String ruleCode;

    @Schema(description = "被检查表名")
    private String checkTableName;

    @Schema(description = "被检查字段名")
    private String checkColumnName;

    @Schema(description = "状态")
    private Integer enableFlag;
}
