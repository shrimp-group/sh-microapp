package com.wkclz.micro.form.bean.entity;

import com.wkclz.core.annotation.FieldDesc;
import com.wkclz.core.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;




/**
 * Description Create by sh-generator
 * @author shrimp
 * @table mdm_form_rule_field (表单校验规则-校验项) 重新生成代码会覆盖
 */

@Data
@EqualsAndHashCode(callSuper = false)
public class MdmFormRuleField extends BaseEntity {

    /**
     * 表单校验规则编码
     */
    @FieldDesc("表单校验规则编码")
    private String formRuleCode;

    /**
     * 字段编码
     */
    @FieldDesc("字段编码")
    private String fieldCode;

    /**
     * 字段名称
     */
    @FieldDesc("字段名称")
    private String fieldName;


    public static MdmFormRuleField copy(MdmFormRuleField source, MdmFormRuleField target) {
        if (target == null ) { target = new MdmFormRuleField();}
        if (source == null) { return target; }
        target.setId(source.getId());
        target.setFormRuleCode(source.getFormRuleCode());
        target.setFieldCode(source.getFieldCode());
        target.setFieldName(source.getFieldName());
        target.setSort(source.getSort());
        target.setCreateTime(source.getCreateTime());
        target.setCreateBy(source.getCreateBy());
        target.setUpdateTime(source.getUpdateTime());
        target.setUpdateBy(source.getUpdateBy());
        target.setRemark(source.getRemark());
        target.setVersion(source.getVersion());
        return target;
    }

    public static MdmFormRuleField copyIfNotNull(MdmFormRuleField source, MdmFormRuleField target) {
        if (target == null ) { target = new MdmFormRuleField();}
        if (source == null) { return target; }
        if (source.getId() != null) { target.setId(source.getId()); }
        if (source.getFormRuleCode() != null) { target.setFormRuleCode(source.getFormRuleCode()); }
        if (source.getFieldCode() != null) { target.setFieldCode(source.getFieldCode()); }
        if (source.getFieldName() != null) { target.setFieldName(source.getFieldName()); }
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

