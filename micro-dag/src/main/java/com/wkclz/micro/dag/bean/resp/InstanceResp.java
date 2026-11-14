package com.wkclz.micro.dag.bean.resp;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class InstanceResp {

    @Schema(description = "实例 id")
    private Long id;

    @Schema(description = "实例流水号")
    private String instanceCode;

    @Schema(description = "跟踪编码")
    private String traceCode;

    @Schema(description = "流程编码")
    private String dagCode;

    @Schema(description = "流程名称")
    private String dagName;

    @Schema(description = "定义版本")
    private Integer dagVersion;

    @Schema(description = "实例状态")
    private String status;

    @Schema(description = "流程上下文 JSON")
    private String contextJson;

    @Schema(description = "发起参数 JSON")
    private String bizParamsJson;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "修改时间")
    private LocalDateTime updateTime;
}
