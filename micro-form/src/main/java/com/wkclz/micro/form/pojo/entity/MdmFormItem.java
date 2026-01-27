package com.wkclz.micro.form.pojo.entity;

import com.wkclz.core.annotation.Desc;
import com.wkclz.core.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;




/**
 * Description Create by shrimp-gen
 * @author wangkaicun
 * @table mdm_form_item (表单-输入项) 重新生成代码会覆盖
 */

@Data
@EqualsAndHashCode(callSuper = false)
public class MdmFormItem extends BaseEntity {

    /**
     * 表单编码
     */
    @Desc("表单编码")
    private String formCode;

    /**
     * 分组
     */
    @Desc("分组")
    private String itemGroup;

    /**
     * 表单输入项编码
     */
    @Desc("表单输入项编码")
    private String itemCode;

    /**
     * 绑定字段名称
     */
    @Desc("绑定字段名称")
    private String itemName;

    /**
     * 输入项类型
     */
    @Desc("输入项类型")
    private String inputType;

    /**
     * 字段类型
     */
    @Desc("字段类型")
    private String fieldType;

    /**
     * 字典类型
     */
    @Desc("字典类型")
    private String dictType;

    /**
     * 输入项标签
     */
    @Desc("输入项标签")
    private String label;

    /**
     * 最小值
     */
    @Desc("最小值")
    private Integer min;

    /**
     * 最大值
     */
    @Desc("最大值")
    private Integer max;

    /**
     * 最小长度
     */
    @Desc("最小长度")
    private Integer minLength;

    /**
     * 最大长度
     */
    @Desc("最大长度")
    private Integer maxLength;

    /**
     * 输入提示
     */
    @Desc("输入提示")
    private String placeholder;

    /**
     * 必填
     */
    @Desc("必填")
    private Integer required;

    /**
     * 默认值
     */
    @Desc("默认值")
    private String defaultValue;

    /**
     * 校验规则(rules)
     */
    @Desc("校验规则(rules)")
    private String rules;

    /**
     * 是否可清除
     */
    @Desc("是否可清除")
    private Integer clearable;


    public static MdmFormItem copy(MdmFormItem source, MdmFormItem target) {
        if (target == null ) { target = new MdmFormItem();}
        if (source == null) { return target; }
        target.setId(source.getId());
        target.setFormCode(source.getFormCode());
        target.setItemGroup(source.getItemGroup());
        target.setItemCode(source.getItemCode());
        target.setItemName(source.getItemName());
        target.setInputType(source.getInputType());
        target.setFieldType(source.getFieldType());
        target.setDictType(source.getDictType());
        target.setLabel(source.getLabel());
        target.setMin(source.getMin());
        target.setMax(source.getMax());
        target.setMinLength(source.getMinLength());
        target.setMaxLength(source.getMaxLength());
        target.setPlaceholder(source.getPlaceholder());
        target.setRequired(source.getRequired());
        target.setDefaultValue(source.getDefaultValue());
        target.setRules(source.getRules());
        target.setClearable(source.getClearable());
        target.setSort(source.getSort());
        target.setCreateTime(source.getCreateTime());
        target.setCreateBy(source.getCreateBy());
        target.setUpdateTime(source.getUpdateTime());
        target.setUpdateBy(source.getUpdateBy());
        target.setRemark(source.getRemark());
        target.setVersion(source.getVersion());
        return target;
    }

    public static MdmFormItem copyIfNotNull(MdmFormItem source, MdmFormItem target) {
        if (target == null ) { target = new MdmFormItem();}
        if (source == null) { return target; }
        if (source.getId() != null) { target.setId(source.getId()); }
        if (source.getFormCode() != null) { target.setFormCode(source.getFormCode()); }
        if (source.getItemGroup() != null) { target.setItemGroup(source.getItemGroup()); }
        if (source.getItemCode() != null) { target.setItemCode(source.getItemCode()); }
        if (source.getItemName() != null) { target.setItemName(source.getItemName()); }
        if (source.getInputType() != null) { target.setInputType(source.getInputType()); }
        if (source.getFieldType() != null) { target.setFieldType(source.getFieldType()); }
        if (source.getDictType() != null) { target.setDictType(source.getDictType()); }
        if (source.getLabel() != null) { target.setLabel(source.getLabel()); }
        if (source.getMin() != null) { target.setMin(source.getMin()); }
        if (source.getMax() != null) { target.setMax(source.getMax()); }
        if (source.getMinLength() != null) { target.setMinLength(source.getMinLength()); }
        if (source.getMaxLength() != null) { target.setMaxLength(source.getMaxLength()); }
        if (source.getPlaceholder() != null) { target.setPlaceholder(source.getPlaceholder()); }
        if (source.getRequired() != null) { target.setRequired(source.getRequired()); }
        if (source.getDefaultValue() != null) { target.setDefaultValue(source.getDefaultValue()); }
        if (source.getRules() != null) { target.setRules(source.getRules()); }
        if (source.getClearable() != null) { target.setClearable(source.getClearable()); }
        if (source.getSort() != null) { target.setSort(source.getSort()); }
        if (source.getCreateTime() != null) { target.setCreateTime(source.getCreateTime()); }
        if (source.getCreateBy() != null) { target.setCreateBy(source.getCreateBy()); }
        if (source.getUpdateTime() != null) { target.setUpdateTime(source.getUpdateTime()); }
        if (source.getUpdateBy() != null) { target.setUpdateBy(source.getUpdateBy()); }
        if (source.getRemark() != null) { target.setRemark(source.getRemark()); }
        if (source.getVersion() != null) { target.setVersion(source.getVersion()); }
        return target;
    }

}

