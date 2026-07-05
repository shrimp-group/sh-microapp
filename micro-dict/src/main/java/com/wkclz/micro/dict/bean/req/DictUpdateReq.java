package com.wkclz.micro.dict.bean.req;

import com.wkclz.web.bean.UpdateReq;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "字典类型修改请求")
public class DictUpdateReq extends UpdateReq {

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
