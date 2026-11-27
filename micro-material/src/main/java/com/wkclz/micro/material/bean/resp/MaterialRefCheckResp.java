package com.wkclz.micro.material.bean.resp;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "素材引用检测结果")
public class MaterialRefCheckResp {

    @Schema(description = "是否已被引用")
    private Boolean referenced;

    @Schema(description = "引用数量")
    private Long count;
}
