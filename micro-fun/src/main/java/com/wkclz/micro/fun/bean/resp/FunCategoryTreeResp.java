package com.wkclz.micro.fun.bean.resp;

import com.wkclz.web.bean.EntityResp;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "函数分类树形响应")
public class FunCategoryTreeResp extends EntityResp {

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

    @Schema(description = "子级分类列表")
    private List<FunCategoryTreeResp> children;
}
