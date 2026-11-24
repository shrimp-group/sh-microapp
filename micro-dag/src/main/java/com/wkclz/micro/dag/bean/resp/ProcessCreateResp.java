package com.wkclz.micro.dag.bean.resp;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/** 新建流程响应 */
@Schema(description = "新建流程响应")
@Data
public class ProcessCreateResp {

    @Schema(description = "流程主表 id")
    private Long id;

    @Schema(description = "流程编码")
    private String dagCode;
}
