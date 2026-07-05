package com.wkclz.micro.material.bean.req;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
@Schema(description = "素材恢复请求")
public class MaterialRestoreReq implements Serializable {

    @NotEmpty(message = "ids 不能为空")
    @Schema(description = "素材ID列表", requiredMode = Schema.RequiredMode.REQUIRED)
    private List<Long> ids;
}
