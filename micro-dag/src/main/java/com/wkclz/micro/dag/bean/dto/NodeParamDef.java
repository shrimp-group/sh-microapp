package com.wkclz.micro.dag.bean.dto;

import com.wkclz.micro.dag.bean.enums.NodeParamType;
import lombok.Data;

import java.util.List;

/**
 * 节点参数定义：handler 代码内声明，随 handler/list 下发给前端渲染表单，并在保存/发布时参与校验
 */
@Data
public class NodeParamDef {

    /** 参数键（params 中的 key） */
    private String key;
    /** 中文名（表单 label） */
    private String label;
    /** 控件类型 */
    private NodeParamType type;
    /** 必填 */
    private boolean required;
    /** 条件必填：params[requiredWhenKey] == requiredWhenValue 时必填 */
    private String requiredWhenKey;
    private String requiredWhenValue;
    /** 默认值（前端表单空值时填充展示，落库为该值） */
    private String defaultValue;
    /** NUMBER 范围 */
    private Long min;
    private Long max;
    /** SELECT 选项 */
    private List<Option> options;
    /** 输入提示 */
    private String placeholder;
    /** 说明 */
    private String help;

    /** SELECT 选项：value 落库，label 展示 */
    @Data
    public static class Option {
        private final String value;
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
