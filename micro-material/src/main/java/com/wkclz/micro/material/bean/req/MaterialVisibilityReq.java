package com.wkclz.micro.material.bean.req;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
@Schema(description = "素材修改可见性请求")
public class MaterialVisibilityReq implements Serializable {

    @NotEmpty(message = "ids 不能为空")
    @Schema(description = "素材ID列表", requiredMode = Schema.RequiredMode.REQUIRED)
    private List<Long> ids;

    @NotBlank(message = "visibility 不能为空")
    @Schema(description = "可见性(PRIVATE/PUBLIC)", requiredMode = Schema.RequiredMode.REQUIRED)
    private String visibility;
}
