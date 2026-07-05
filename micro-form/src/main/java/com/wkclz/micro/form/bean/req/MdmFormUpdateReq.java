package com.wkclz.micro.form.bean.req;

import com.wkclz.web.bean.UpdateReq;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "表单修改请求")
public class MdmFormUpdateReq extends UpdateReq {

    @Schema(description = "表单编码")
    private String formCode;

    @NotBlank(message = "表单名称不能为空")
    @Schema(description = "表单名称", requiredMode = Schema.RequiredMode.REQUIRED)
    private String formName;

    @Schema(description = "排序")
    private Integer sort;

    @Schema(description = "备注")
    private String remark;

    @Valid
    @Schema(description = "表单项列表")
    private List<MdmFormItemReq> items;

    @Data
    @Schema(description = "表单输入项")
    public static class MdmFormItemReq implements Serializable {

        @Schema(description = "分组")
        private String itemGroup;

        @Schema(description = "表单输入项编码")
        private String itemCode;

        @NotNull(message = "输入项类型不能为空")
        @Schema(description = "输入项类型", requiredMode = Schema.RequiredMode.REQUIRED)
        private String inputType;

        @NotNull(message = "字段类型不能为空")
        @Schema(description = "字段类型", requiredMode = Schema.RequiredMode.REQUIRED)
        private String fieldType;

        @Schema(description = "字典类型")
        private String dictType;

        @NotNull(message = "表单项名称不能为空")
        @Schema(description = "绑定字段名称", requiredMode = Schema.RequiredMode.REQUIRED)
        private String itemName;

        @NotNull(message = "输入项标签不能为空")
        @Schema(description = "输入项标签", requiredMode = Schema.RequiredMode.REQUIRED)
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

        @Schema(description = "排序")
        private Integer sort;
    }
}
