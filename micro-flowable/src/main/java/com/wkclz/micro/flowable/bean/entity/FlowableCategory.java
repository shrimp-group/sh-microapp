package com.wkclz.micro.flowable.bean.entity;

import com.wkclz.core.base.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
@Schema(description = "流程分类")
public class FlowableCategory extends BaseEntity {

    @Schema(description = "分类编码")
    private String categoryCode;

    @Schema(description = "分类名称")
    private String categoryName;

    @Schema(description = "有效状态")
    private Integer status;
}
