package com.wkclz.micro.dict.bean.resp;

import com.wkclz.web.bean.EntityResp;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "字典复制响应")
public class DictCopyResp extends EntityResp {

    @Schema(description = "字典分类")
    private String dictCtg;

    @Schema(description = "字典类型")
    private String dictType;

    @Schema(description = "描述信息")
    private String description;

    @Schema(description = "字典项列表")
    private List<DictItemResp> items;
}
