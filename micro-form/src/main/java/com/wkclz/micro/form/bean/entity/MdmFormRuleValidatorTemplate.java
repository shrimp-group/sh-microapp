package com.wkclz.micro.form.bean.entity;

import com.wkclz.core.annotation.FieldDesc;
import com.wkclz.core.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;




/**
 * Description Create by sh-generator
 * @author shrimp
 * @table mdm_form_rule_validator_template (表单校验规则-模板) 重新生成代码会覆盖
 */

@Data
@EqualsAndHashCode(callSuper = false)
public class MdmFormRuleValidatorTemplate extends BaseEntity {

    /**
     * 模板编码
     */
    @FieldDesc("模板编码")
    private String templateCode;

    /**
     * 模板名称
     */
    @FieldDesc("模板名称")
    private String templateName;

    /**
     * 表单校验正则
     */
    @FieldDesc("表单校验正则")
    private String validatorPattern;

    /**
     * 表单验证函数
     */
    @FieldDesc("表单验证函数")
    private String validatorFunction;


    public static MdmFormRuleValidatorTemplate copy(MdmFormRuleValidatorTemplate source, MdmFormRuleValidatorTemplate target) {
        if (target == null ) { target = new MdmFormRuleValidatorTemplate();}
        if (source == null) { return target; }
        target.setId(source.getId());
        target.setTemplateCode(source.getTemplateCode());
        target.setTemplateName(source.getTemplateName());
        target.setValidatorPattern(source.getValidatorPattern());
        target.setValidatorFunction(source.getValidatorFunction());
        target.setSort(source.getSort());
        target.setCreateTime(source.getCreateTime());
        target.setCreateBy(source.getCreateBy());
        target.setUpdateTime(source.getUpdateTime());
        target.setUpdateBy(source.getUpdateBy());
        target.setRemark(source.getRemark());
        target.setVersion(source.getVersion());
        return target;
    }

    public static MdmFormRuleValidatorTemplate copyIfNotNull(MdmFormRuleValidatorTemplate source, MdmFormRuleValidatorTemplate target) {
        if (target == null ) { target = new MdmFormRuleValidatorTemplate();}
        if (source == null) { return target; }
        if (source.getId() != null) { target.setId(source.getId()); }
        if (source.getTemplateCode() != null) { target.setTemplateCode(source.getTemplateCode()); }
        if (source.getTemplateName() != null) { target.setTemplateName(source.getTemplateName()); }
        if (source.getValidatorPattern() != null) { target.setValidatorPattern(source.getValidatorPattern()); }
        if (source.getValidatorFunction() != null) { target.setValidatorFunction(source.getValidatorFunction()); }
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

