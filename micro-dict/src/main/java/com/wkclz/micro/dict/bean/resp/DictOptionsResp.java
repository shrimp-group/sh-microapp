package com.wkclz.micro.dict.bean.resp;

import com.wkclz.web.bean.EntityResp;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "字典类型选项响应")
public class DictOptionsResp extends EntityResp {

    @Schema(description = "字典类型")
    private String dictType;

    @Schema(description = "描述信息")
    private String description;

    @Schema(description = "排序")
    private Integer sort;
}
