package com.wkclz.micro.dict.bean.req;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.io.Serializable;

@Data
@Schema(description = "公共多字典查询请求")
public class CommonDictsListReq implements Serializable {

    @NotBlank(message = "字典类型不能为空")
    @Schema(description = "字典类型，英文逗号分隔", requiredMode = Schema.RequiredMode.REQUIRED)
    private String dictType;
}
