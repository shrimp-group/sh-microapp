package com.wkclz.micro.rmcheck.bean.req;

import com.wkclz.web.bean.UpdateReq;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "删除检查规则修改请求")
public class RmCheckRuleUpdateReq extends UpdateReq {

    @NotBlank(message = "表名不能为空")
    @Schema(description = "表名", requiredMode = Schema.RequiredMode.REQUIRED)
    private String tableName;

    @NotBlank(message = "字段名不能为空")
    @Schema(description = "字段名", requiredMode = Schema.RequiredMode.REQUIRED)
    private String columnName;

    @Schema(description = "状态")
    private Integer enableFlag;

    @Schema(description = "排序")
    private Integer sort;

    @Schema(description = "备注")
    private String remark;
}
