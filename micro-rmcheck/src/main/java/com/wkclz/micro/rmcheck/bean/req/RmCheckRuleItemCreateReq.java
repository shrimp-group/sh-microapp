package com.wkclz.micro.rmcheck.bean.req;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.io.Serializable;

@Data
@Schema(description = "删除检查规则检查项创建请求")
public class RmCheckRuleItemCreateReq implements Serializable {

    @NotBlank(message = "规则编码不能为空")
    @Schema(description = "规则编码", requiredMode = Schema.RequiredMode.REQUIRED)
    private String ruleCode;

    @NotBlank(message = "被检查表名不能为空")
    @Schema(description = "被检查表名", requiredMode = Schema.RequiredMode.REQUIRED)
    private String checkTableName;

    @NotBlank(message = "被检查字段名不能为空")
    @Schema(description = "被检查字段名", requiredMode = Schema.RequiredMode.REQUIRED)
    private String checkColumnName;

    @Schema(description = "提示信息")
    private String noticeMessage;

    @Schema(description = "状态")
    private Integer enableFlag;

    @Schema(description = "排序")
    private Integer sort;

    @Schema(description = "备注")
    private String remark;
}
