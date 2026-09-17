package com.wkclz.micro.flowable.bean.req;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "流程分类修改请求")
public class CategoryUpdateReq {
    @NotNull(message = "id 不能为空")
    @Schema(description = "分类 ID")
    private Long id;

    @NotNull(message = "version 不能为空")
    @Schema(description = "乐观锁版本")
    private Integer version;

    @Schema(description = "分类名称")
    private String categoryName;

    @Schema(description = "有效状态")
    private Integer status;

    @Schema(description = "排序")
    private Integer sort;

    @Schema(description = "备注")
    private String remark;
}
