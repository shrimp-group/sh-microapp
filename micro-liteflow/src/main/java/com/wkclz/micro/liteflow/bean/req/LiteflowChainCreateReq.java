package com.wkclz.micro.liteflow.bean.req;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;

@Data
@Schema(description = "规则链创建请求")
public class LiteflowChainCreateReq implements Serializable {

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
