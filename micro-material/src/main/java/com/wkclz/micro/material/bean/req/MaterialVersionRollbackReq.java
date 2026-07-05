package com.wkclz.micro.material.bean.req;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.io.Serializable;

@Data
@Schema(description = "素材版本回滚请求")
public class MaterialVersionRollbackReq implements Serializable {

    @NotBlank(message = "materialCode 不能为空")
    @Schema(description = "素材编码", requiredMode = Schema.RequiredMode.REQUIRED)
    private String materialCode;
}
