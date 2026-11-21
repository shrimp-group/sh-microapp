package com.wkclz.micro.dag.bean.resp;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/** 流程版本元信息响应 */
@Data
public class ProcessVersionResp {

    @Schema(description = "版本 id")
    private Long id;

    @Schema(description = "流程编码")
    private String dagCode;

    @Schema(description = "版本号")
    private Integer dagVersion;

    @Schema(description = "状态：DRAFT/PUBLISHED")
    private String status;

    @Schema(description = "更新时间")
    private LocalDateTime updateTime;
}
