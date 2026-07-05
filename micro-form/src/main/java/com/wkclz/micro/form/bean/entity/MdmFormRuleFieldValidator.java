package com.wkclz.micro.form.bean.entity;

import com.wkclz.core.annotation.FieldDesc;
import com.wkclz.core.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;




/**
 * Description Create by sh-generator
 * @author shrimp
 * @table mdm_form_rule_field_validator (表单校验规则-验证器) 重新生成代码会覆盖
 */

@Data
@EqualsAndHashCode(callSuper = false)
public class MdmFormRuleFieldValidator extends BaseEntity {

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
     * 验证器类型
     */
    @FieldDesc("验证器类型")
    private String validatorType;

    /**
     * 验证匹配器
     */
    @FieldDesc("验证匹配器")
    private String validatorPattern;

    /**
     * 验证函数
     */
    @FieldDesc("验证函数")
    private String validatorFunction;

    /**
     * 模板编码
     */
    @FieldDesc("模板编码")
    private String templateCode;

    /**
     * 验证消息模板
     */
    @FieldDesc("验证消息模板")
    private String msgTemplate;


    public static MdmFormRuleFieldValidator copy(MdmFormRuleFieldValidator source, MdmFormRuleFieldValidator target) {
        if (target == null ) { target = new MdmFormRuleFieldValidator();}
        if (source == null) { return target; }
        target.setId(source.getId());
        target.setFormRuleCode(source.getFormRuleCode());
        target.setFieldCode(source.getFieldCode());
        target.setValidatorType(source.getValidatorType());
        target.setValidatorPattern(source.getValidatorPattern());
        target.setValidatorFunction(source.getValidatorFunction());
        target.setTemplateCode(source.getTemplateCode());
        target.setMsgTemplate(source.getMsgTemplate());
        target.setSort(source.getSort());
        target.setCreateTime(source.getCreateTime());
        target.setCreateBy(source.getCreateBy());
        target.setUpdateTime(source.getUpdateTime());
        target.setUpdateBy(source.getUpdateBy());
        target.setRemark(source.getRemark());
        target.setVersion(source.getVersion());
        return target;
    }

    public static MdmFormRuleFieldValidator copyIfNotNull(MdmFormRuleFieldValidator source, MdmFormRuleFieldValidator target) {
        if (target == null ) { target = new MdmFormRuleFieldValidator();}
        if (source == null) { return target; }
        if (source.getId() != null) { target.setId(source.getId()); }
        if (source.getFormRuleCode() != null) { target.setFormRuleCode(source.getFormRuleCode()); }
        if (source.getFieldCode() != null) { target.setFieldCode(source.getFieldCode()); }
        if (source.getValidatorType() != null) { target.setValidatorType(source.getValidatorType()); }
        if (source.getValidatorPattern() != null) { target.setValidatorPattern(source.getValidatorPattern()); }
        if (source.getValidatorFunction() != null) { target.setValidatorFunction(source.getValidatorFunction()); }
        if (source.getTemplateCode() != null) { target.setTemplateCode(source.getTemplateCode()); }
        if (source.getMsgTemplate() != null) { target.setMsgTemplate(source.getMsgTemplate()); }
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

