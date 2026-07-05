package com.wkclz.micro.material.bean.req;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.io.Serializable;

@Data
@Schema(description = "素材引用注册请求")
public class MaterialRefBindReq implements Serializable {

    @NotBlank(message = "materialCode 不能为空")
    @Schema(description = "素材编码", requiredMode = Schema.RequiredMode.REQUIRED)
    private String materialCode;

    @NotBlank(message = "bizType 不能为空")
    @Schema(description = "业务类型", requiredMode = Schema.RequiredMode.REQUIRED)
    private String bizType;

    @NotBlank(message = "bizCode 不能为空")
    @Schema(description = "业务编码", requiredMode = Schema.RequiredMode.REQUIRED)
    private String bizCode;

    @Schema(description = "引用描述")
    private String refDesc;
}
