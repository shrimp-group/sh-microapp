package com.wkclz.micro.form.pojo.entity;

import com.wkclz.core.annotation.Desc;
import com.wkclz.core.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;




/**
 * Description Create by shrimp-gen
 * @author wangkaicun
 * @table mdm_form_rule (表单校验规则) 重新生成代码会覆盖
 */

@Data
@EqualsAndHashCode(callSuper = false)
public class MdmFormRule extends BaseEntity {

    /**
     * 表单校验规则编码
     */
    @Desc("表单校验规则编码")
    private String formRuleCode;

    /**
     * 表单校验规则名称
     */
    @Desc("表单校验规则名称")
    private String formRuleName;

    /**
     * API 方法
     */
    @Desc("API 方法")
    private String apiMethod;

    /**
     * API 路径
     */
    @Desc("API 路径")
    private String apiUri;


    public static MdmFormRule copy(MdmFormRule source, MdmFormRule target) {
        if (target == null ) { target = new MdmFormRule();}
        if (source == null) { return target; }
        target.setId(source.getId());
        target.setFormRuleCode(source.getFormRuleCode());
        target.setFormRuleName(source.getFormRuleName());
        target.setApiMethod(source.getApiMethod());
        target.setApiUri(source.getApiUri());
        target.setSort(source.getSort());
        target.setCreateTime(source.getCreateTime());
        target.setCreateBy(source.getCreateBy());
        target.setUpdateTime(source.getUpdateTime());
        target.setUpdateBy(source.getUpdateBy());
        target.setRemark(source.getRemark());
        target.setVersion(source.getVersion());
        return target;
    }

    public static MdmFormRule copyIfNotNull(MdmFormRule source, MdmFormRule target) {
        if (target == null ) { target = new MdmFormRule();}
        if (source == null) { return target; }
        if (source.getId() != null) { target.setId(source.getId()); }
        if (source.getFormRuleCode() != null) { target.setFormRuleCode(source.getFormRuleCode()); }
        if (source.getFormRuleName() != null) { target.setFormRuleName(source.getFormRuleName()); }
        if (source.getApiMethod() != null) { target.setApiMethod(source.getApiMethod()); }
        if (source.getApiUri() != null) { target.setApiUri(source.getApiUri()); }
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

