package com.wkclz.micro.material.bean.req;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
@Schema(description = "素材分组排序请求")
public class MaterialGroupSortReq implements Serializable {

    @NotEmpty(message = "ids 不能为空")
    @Schema(description = "分组ID列表(按排序顺序)", requiredMode = Schema.RequiredMode.REQUIRED)
    private List<Long> ids;
}
