package com.wkclz.micro.dag.bean.req;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class NodeInterveneReq {

    @Schema(description = "实例流水号")
    private String instanceCode;

    @Schema(description = "节点编码")
    private String nodeCode;

    @Schema(description = "跳过原因（skip）")
    private String skipReason;

    @Schema(description = "节点参数 JSON（params）")
    private String inputParamsJson;

    @Schema(description = "重置时是否清除节点参数（reset，默认 false）")
    private Boolean clearParams;
}
