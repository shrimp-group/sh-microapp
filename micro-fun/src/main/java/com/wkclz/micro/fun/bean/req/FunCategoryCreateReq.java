package com.wkclz.micro.fun.bean.req;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;

@Data
@Schema(description = "函数分类创建请求")
public class FunCategoryCreateReq implements Serializable {

    @NotBlank(message = "pcode不能为空")
    @Schema(description = "父类Code,0为顶级", requiredMode = Schema.RequiredMode.REQUIRED)
    private String pcode;

    @NotBlank(message = "分类名称不能为空")
    @Schema(description = "分类名称", requiredMode = Schema.RequiredMode.REQUIRED)
    private String categoryName;

    @Schema(description = "描述")
    private String description;

    @Schema(description = "可见1/0")
    private Integer visible;

    @Schema(description = "排序")
    private Integer sort;

    @Schema(description = "备注")
    private String remark;
}
