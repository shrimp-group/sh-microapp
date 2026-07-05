package com.wkclz.micro.dict.bean.resp;

import com.wkclz.web.bean.EntityResp;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "字典类型分页响应")
public class DictPageResp extends EntityResp {

    @Schema(description = "字典分类")
    private String dictCtg;

    @Schema(description = "字典类型")
    private String dictType;

    @Schema(description = "描述信息")
    private String description;
}
