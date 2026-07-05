package com.wkclz.micro.material.bean.req;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.io.Serializable;

@Data
@Schema(description = "素材链接有效性检测请求")
public class MaterialLinkCheckReq implements Serializable {

    @NotBlank(message = "linkUrl 不能为空")
    @Schema(description = "链接地址", requiredMode = Schema.RequiredMode.REQUIRED)
    private String linkUrl;
}
