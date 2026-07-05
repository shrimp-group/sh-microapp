package com.wkclz.micro.dict.bean.req;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
@Schema(description = "字典粘贴请求")
public class DictPasteReq implements Serializable {

    @Schema(description = "粘贴数据列表")
    private List<DictPasteItem> list;

    @Data
    @Schema(description = "字典粘贴项")
    public static class DictPasteItem implements Serializable {

        @Schema(description = "字典分类")
        private String dictCtg;

        @Schema(description = "字典类型")
        private String dictType;

        @Schema(description = "描述信息")
        private String description;

        @Schema(description = "排序")
        private Integer sort;

        @Schema(description = "备注")
        private String remark;

        @Schema(description = "字典项列表")
        private List<DictPasteDictItem> items;
    }

    @Data
    @Schema(description = "字典粘贴字典项")
    public static class DictPasteDictItem implements Serializable {

        @Schema(description = "字典类型")
        private String dictType;

        @Schema(description = "字典值")
        private String dictValue;

        @Schema(description = "字典标签")
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
