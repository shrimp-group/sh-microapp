package com.wkclz.micro.material.bean.req;

import com.wkclz.web.bean.UpdateReq;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "素材分组移动请求")
public class MaterialGroupMoveReq extends UpdateReq {

    @NotBlank(message = "parentCode 不能为空")
    @Schema(description = "目标父级分组编码(顶级为0)", requiredMode = Schema.RequiredMode.REQUIRED)
    private String parentCode;
}
