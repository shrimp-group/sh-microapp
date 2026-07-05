package com.wkclz.micro.dict.bean.resp;

import com.wkclz.web.bean.EntityResp;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "字典项响应")
public class DictItemResp extends EntityResp {

    @Schema(description = "字典类型")
    private String dictType;

    @Schema(description = "字典值")
    private String dictValue;

    @Schema(description = "字典标签")
    private String dictLabel;

    @Schema(description = "el类型")
    private String elType;

    @Schema(description = "描述")
    private String description;

    @Schema(description = "生效状态")
    private Integer enableFlag;
}
