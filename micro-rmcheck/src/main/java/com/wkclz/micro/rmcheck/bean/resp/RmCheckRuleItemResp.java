package com.wkclz.micro.rmcheck.bean.resp;

import com.wkclz.web.bean.EntityResp;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "删除检查规则检查项响应")
public class RmCheckRuleItemResp extends EntityResp {

    @Schema(description = "规则编码")
    private String ruleCode;

    @Schema(description = "被检查表名")
    private String checkTableName;

    @Schema(description = "被检查字段名")
    private String checkColumnName;

    @Schema(description = "提示信息")
    private String noticeMessage;

    @Schema(description = "状态")
    private Integer enableFlag;

    @Schema(description = "被检查表注释")
    private String checkTableComment;

    @Schema(description = "被检查字段注释")
    private String checkColumnComment;
}
