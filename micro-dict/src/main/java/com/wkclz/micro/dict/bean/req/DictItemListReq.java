package com.wkclz.micro.dict.bean.req;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.io.Serializable;

@Data
@Schema(description = "字典项列表查询请求")
public class DictItemListReq implements Serializable {

    @NotBlank(message = "字典类型不能为空")
    @Schema(description = "字典类型【可全下划线大写，可小写驼峰】", requiredMode = Schema.RequiredMode.REQUIRED)
    private String dictType;

    @Schema(description = "字典标签")
    private String dictLabel;

    @Schema(description = "字典值【支持模糊查询】")
    private String dictValue;

    @Schema(description = "描述【支持模糊查询】")
    private String description;

    @Schema(description = "生效状态")
    private Integer enableFlag;
}
