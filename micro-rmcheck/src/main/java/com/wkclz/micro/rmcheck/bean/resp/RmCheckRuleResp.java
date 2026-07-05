package com.wkclz.micro.rmcheck.bean.resp;

import com.wkclz.web.bean.EntityResp;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "删除检查规则响应")
public class RmCheckRuleResp extends EntityResp {

    @Schema(description = "规则编码")
    private String ruleCode;

    @Schema(description = "表名")
    private String tableName;

    @Schema(description = "字段名")
    private String columnName;

    @Schema(description = "状态")
    private Integer enableFlag;
}
