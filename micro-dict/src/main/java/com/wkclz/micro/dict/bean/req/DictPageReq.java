package com.wkclz.micro.dict.bean.req;

import com.wkclz.web.bean.PageReq;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "字典类型分页查询请求")
public class DictPageReq extends PageReq {

    @Schema(description = "字典分类")
    private String dictCtg;

    @Schema(description = "字典类型【支持模糊查询】")
    private String dictType;

    @Schema(description = "描述信息【支持模糊查询】")
    private String description;
}
