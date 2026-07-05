package com.wkclz.micro.liteflow.bean.resp;

import com.wkclz.web.bean.EntityResp;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "规则链响应")
public class LiteflowChainResp extends EntityResp {

    @Schema(description = "规则名称")
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
}
