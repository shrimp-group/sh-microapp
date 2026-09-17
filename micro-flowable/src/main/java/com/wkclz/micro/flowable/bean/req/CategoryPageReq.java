package com.wkclz.micro.flowable.bean.req;

import com.wkclz.web.bean.PageReq;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "流程分类分页查询")
public class CategoryPageReq extends PageReq {
    @Schema(description = "分类编码（模糊）")
    private String categoryCode;
    @Schema(description = "分类名称（模糊）")
    private String categoryName;
    @Schema(description = "有效状态")
    private Integer status;
}
