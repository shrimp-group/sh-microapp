package com.wkclz.micro.fun.bean.req;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

@Data
@Schema(description = "函数分类列表查询请求")
public class FunCategoryListReq implements Serializable {

    @Schema(description = "父类Code,0为顶级")
    private String pcode;

    @Schema(description = "分类编码")
    private String categoryCode;

    @Schema(description = "分类名称")
    private String categoryName;

    @Schema(description = "描述")
    private String description;

    @Schema(description = "可见1/0")
    private Integer visible;
}
