package com.wkclz.micro.flowable.bean.resp;

import com.wkclz.web.bean.EntityResp;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "流程分类响应")
public class CategoryResp extends EntityResp {
    @Schema(description = "分类编码")
    private String categoryCode;
    @Schema(description = "分类名称")
    private String categoryName;
    @Schema(description = "有效状态")
    private Integer status;
}
