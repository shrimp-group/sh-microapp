package com.wkclz.micro.dag.bean.req;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Map;

@Schema(description = "发起流程实例请求")
@Data
public class LaunchReq {

    @Schema(description = "流程编码")
    private String dagCode;

    @Schema(description = "跟踪编码，可空（运行期由 handler 回填）")
    private String traceCode;

    @Schema(description = "发起参数，同时作为流程上下文 ${ctx.*}")
    private Map<String, Object> params;
}
