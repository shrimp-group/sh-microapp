package com.wkclz.micro.dag.bean.dto;

import com.wkclz.micro.dag.bean.enums.NodeParamType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 节点参数定义：handler 代码内声明，随 handler/list 下发给前端渲染表单，并在保存/发布时参与校验
 */
@Schema(description = "节点参数定义：随 handler 下发给前端渲染表单，保存/发布时参与校验")
@Data
public class NodeParamDef {

    /** 参数键（params 中的 key） */
    @Schema(description = "参数键(params 中的 key)")
    private String key;
    /** 中文名（表单 label） */
    @Schema(description = "中文名(表单 label)")
    private String label;
    /** 控件类型 */
    @Schema(description = "控件类型")
    private NodeParamType type;
    /** 必填 */
    @Schema(description = "是否必填")
    private boolean required;
    /** 条件必填：params[requiredWhenKey] == requiredWhenValue 时必填 */
    @Schema(description = "条件必填的参数键")
    private String requiredWhenKey;
    @Schema(description = "条件必填的匹配值")
    private String requiredWhenValue;
    /** 默认值（前端表单空值时填充展示，落库为该值） */
    @Schema(description = "默认值(前端表单空值时填充展示，落库为该值)")
    private String defaultValue;
    /** NUMBER 范围 */
    @Schema(description = "NUMBER 最小值")
    private Long min;
    @Schema(description = "NUMBER 最大值")
    private Long max;
    /** SELECT 选项 */
    @Schema(description = "SELECT 选项")
    private List<Option> options;
    /** 输入提示 */
    @Schema(description = "输入提示")
    private String placeholder;
    /** 说明 */
    @Schema(description = "说明")
    private String help;

    /** SELECT 选项：value 落库，label 展示 */
    @Schema(description = "SELECT 选项：value 落库，label 展示")
    @Data
    public static class Option {
        @Schema(description = "选项值(落库)")
        private final String value;
        @Schema(description = "选项文案(展示)")
        private final String label;
    }

    public static NodeParamDef of(String key, String label, NodeParamType type) {
        NodeParamDef def = new NodeParamDef();
        def.key = key;
        def.label = label;
        def.type = type;
        return def;
    }

    public NodeParamDef required() {
        this.required = true;
        return this;
    }

    public NodeParamDef requiredWhen(String key, String value) {
        this.requiredWhenKey = key;
        this.requiredWhenValue = value;
        return this;
    }

    public NodeParamDef range(Long min, Long max) {
        this.min = min;
        this.max = max;
        return this;
    }

    public NodeParamDef options(Option... options) {
        this.options = List.of(options);
        return this;
    }

    public NodeParamDef defaultValue(String v) {
        this.defaultValue = v;
        return this;
    }

    public NodeParamDef placeholder(String v) {
        this.placeholder = v;
        return this;
    }

    public NodeParamDef help(String v) {
        this.help = v;
        return this;
    }
}
