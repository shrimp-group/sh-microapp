package com.wkclz.micro.dict.bean.req;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.io.Serializable;

@Data
@Schema(description = "字典类型创建请求")
public class DictCreateReq implements Serializable {

    @NotBlank(message = "字典分类不能为空")
    @Schema(description = "字典分类", requiredMode = Schema.RequiredMode.REQUIRED)
    private String dictCtg;

    @NotBlank(message = "字典类型不能为空")
    @Schema(description = "字典类型", requiredMode = Schema.RequiredMode.REQUIRED)
    private String dictType;

    @Schema(description = "描述信息")
    private String description;

    @Schema(description = "排序")
    private Integer sort;

    @Schema(description = "备注")
    private String remark;
}
