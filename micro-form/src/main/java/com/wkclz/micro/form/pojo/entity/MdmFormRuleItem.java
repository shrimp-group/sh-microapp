package com.wkclz.micro.form.pojo.entity;

import com.wkclz.core.annotation.Desc;
import com.wkclz.core.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;




/**
 * Description Create by shrimp-gen
 * @author wangkaicun
 * @table mdm_form_rule_item (表单校验规则-校验项) 重新生成代码会覆盖
 */

@Data
@EqualsAndHashCode(callSuper = false)
public class MdmFormRuleItem extends BaseEntity {

    /**
     * 表单校验规则编码
     */
    @Desc("表单校验规则编码")
    private String formRuleCode;

    /**
     * 字段名称
     */
    @Desc("字段名称")
    private String fieldName;

    /**
     * 必填
     */
    @Desc("必填")
    private Integer required;

    /**
     * 字段类型
     */
    @Desc("字段类型")
    private String fieldType;

    /**
     * 最小值
     */
    @Desc("最小值")
    private Double dataMin;

    /**
     * 最大值
     */
    @Desc("最大值")
    private Double dataMax;

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
     * 表单校验正则
     */
    @Desc("表单校验正则")
    private String pattern;

    /**
     * 表单验证函数
     */
    @Desc("表单验证函数")
    private String validator;

    /**
     * 触发方式
     */
    @Desc("触发方式")
    private String ruleTrigger;

    /**
     * 验证失败提示
     */
    @Desc("验证失败提示")
    private String message;


    public static MdmFormRuleItem copy(MdmFormRuleItem source, MdmFormRuleItem target) {
        if (target == null ) { target = new MdmFormRuleItem();}
        if (source == null) { return target; }
        target.setId(source.getId());
        target.setFormRuleCode(source.getFormRuleCode());
        target.setFieldName(source.getFieldName());
        target.setRequired(source.getRequired());
        target.setFieldType(source.getFieldType());
        target.setDataMin(source.getDataMin());
        target.setDataMax(source.getDataMax());
        target.setMinLength(source.getMinLength());
        target.setMaxLength(source.getMaxLength());
        target.setPattern(source.getPattern());
        target.setValidator(source.getValidator());
        target.setRuleTrigger(source.getRuleTrigger());
        target.setMessage(source.getMessage());
        target.setSort(source.getSort());
        target.setCreateTime(source.getCreateTime());
        target.setCreateBy(source.getCreateBy());
        target.setUpdateTime(source.getUpdateTime());
        target.setUpdateBy(source.getUpdateBy());
        target.setRemark(source.getRemark());
        target.setVersion(source.getVersion());
        return target;
    }

    public static MdmFormRuleItem copyIfNotNull(MdmFormRuleItem source, MdmFormRuleItem target) {
        if (target == null ) { target = new MdmFormRuleItem();}
        if (source == null) { return target; }
        if (source.getId() != null) { target.setId(source.getId()); }
        if (source.getFormRuleCode() != null) { target.setFormRuleCode(source.getFormRuleCode()); }
        if (source.getFieldName() != null) { target.setFieldName(source.getFieldName()); }
        if (source.getRequired() != null) { target.setRequired(source.getRequired()); }
        if (source.getFieldType() != null) { target.setFieldType(source.getFieldType()); }
        if (source.getDataMin() != null) { target.setDataMin(source.getDataMin()); }
        if (source.getDataMax() != null) { target.setDataMax(source.getDataMax()); }
        if (source.getMinLength() != null) { target.setMinLength(source.getMinLength()); }
        if (source.getMaxLength() != null) { target.setMaxLength(source.getMaxLength()); }
        if (source.getPattern() != null) { target.setPattern(source.getPattern()); }
        if (source.getValidator() != null) { target.setValidator(source.getValidator()); }
        if (source.getRuleTrigger() != null) { target.setRuleTrigger(source.getRuleTrigger()); }
        if (source.getMessage() != null) { target.setMessage(source.getMessage()); }
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

