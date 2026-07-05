package com.wkclz.micro.dict.bean.req;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
@Schema(description = "字典项保存请求")
public class DictItemSaveReq implements Serializable {

    @NotBlank(message = "字典类型不能为空")
    @Schema(description = "字典类型", requiredMode = Schema.RequiredMode.REQUIRED)
    private String dictType;

    @Schema(description = "字典项列表")
    private List<DictItemSaveItem> items;

    @Data
    @Schema(description = "字典项保存项")
    public static class DictItemSaveItem implements Serializable {

        @NotBlank(message = "字典值不能为空")
        @Schema(description = "字典值", requiredMode = Schema.RequiredMode.REQUIRED)
        private String dictValue;

        @NotBlank(message = "字典标签不能为空")
        @Schema(description = "字典标签", requiredMode = Schema.RequiredMode.REQUIRED)
        private String dictLabel;

        @Schema(description = "el类型")
        private String elType;

        @Schema(description = "描述")
        private String description;

        @Schema(description = "生效状态")
        private Integer enableFlag;

        @Schema(description = "排序")
        private Integer sort;
    }
}
