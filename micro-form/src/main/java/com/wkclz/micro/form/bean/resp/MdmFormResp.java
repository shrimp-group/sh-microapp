package com.wkclz.micro.form.bean.resp;

import com.wkclz.web.bean.EntityResp;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "表单响应")
public class MdmFormResp extends EntityResp {

    @Schema(description = "表单编码")
    private String formCode;

    @Schema(description = "表单名称")
    private String formName;

    @Schema(description = "表单项列表")
    private List<MdmFormItemResp> items;

    @Data
    @EqualsAndHashCode(callSuper = true)
    @Schema(description = "表单输入项响应")
    public static class MdmFormItemResp extends EntityResp {

        @Schema(description = "表单编码")
        private String formCode;

        @Schema(description = "分组")
        private String itemGroup;

        @Schema(description = "表单输入项编码")
        private String itemCode;

        @Schema(description = "绑定字段名称")
        private String itemName;

        @Schema(description = "输入项类型")
        private String inputType;

        @Schema(description = "字段类型")
        private String fieldType;

        @Schema(description = "字典类型")
        private String dictType;

        @Schema(description = "输入项标签")
        private String label;

        @Schema(description = "最小值")
        private Integer min;

        @Schema(description = "最大值")
        private Integer max;

        @Schema(description = "最小长度")
        private Integer minLength;

        @Schema(description = "最大长度")
        private Integer maxLength;

        @Schema(description = "输入提示")
        private String placeholder;

        @Schema(description = "必填")
        private Integer required;

        @Schema(description = "默认值")
        private String defaultValue;

        @Schema(description = "校验规则")
        private String rules;

        @Schema(description = "是否可清除")
        private Integer clearable;
    }
}
