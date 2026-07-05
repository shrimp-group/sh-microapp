package com.wkclz.micro.liteflow.bean.req;

import com.wkclz.web.bean.UpdateReq;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "规则链修改请求")
public class LiteflowChainUpdateReq extends UpdateReq {

    @NotBlank(message = "规则名称不能为空")
    @Schema(description = "规则名称", requiredMode = Schema.RequiredMode.REQUIRED)
    private String chainName;

    @Schema(description = "规则描述")
    private String chainDesc;

    @Schema(description = "规则数据")
    private String elData;

    @Schema(description = "路由")
    private String route;

    @Schema(description = "命名空间")
    private String namespace;

    @Schema(description = "状态")
    private Integer enable;

    @Schema(description = "排序")
    private Integer sort;

    @Schema(description = "备注")
    private String remark;
}
