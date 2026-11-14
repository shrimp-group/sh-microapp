package com.wkclz.micro.dag.bean.resp;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class HandlerInfoResp {

    @Schema(description = "handler 编码")
    private String code;

    @Schema(description = "handler 名称")
    private String name;
}
