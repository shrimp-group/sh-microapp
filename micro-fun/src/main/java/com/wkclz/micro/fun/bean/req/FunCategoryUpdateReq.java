package com.wkclz.micro.fun.bean.req;

import com.wkclz.web.bean.UpdateReq;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "函数分类修改请求")
public class FunCategoryUpdateReq extends UpdateReq {

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
