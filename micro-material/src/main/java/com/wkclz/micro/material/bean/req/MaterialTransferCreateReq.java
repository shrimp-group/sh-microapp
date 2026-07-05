package com.wkclz.micro.material.bean.req;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
@Schema(description = "素材转移请求")
public class MaterialTransferCreateReq implements Serializable {

    @NotEmpty(message = "ids 不能为空")
    @Schema(description = "素材ID列表", requiredMode = Schema.RequiredMode.REQUIRED)
    private List<Long> ids;

    @NotBlank(message = "toUserCode 不能为空")
    @Schema(description = "目标用户编码", requiredMode = Schema.RequiredMode.REQUIRED)
    private String toUserCode;
}
