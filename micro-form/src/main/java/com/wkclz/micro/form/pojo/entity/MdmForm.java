package com.wkclz.micro.form.pojo.entity;

import com.wkclz.core.annotation.Desc;
import com.wkclz.core.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;




/**
 * Description Create by shrimp-gen
 * @author wangkaicun
 * @table mdm_form (表单) 重新生成代码会覆盖
 */

@Data
@EqualsAndHashCode(callSuper = false)
public class MdmForm extends BaseEntity {

    /**
     * 表单编码
     */
    @Desc("表单编码")
    private String formCode;

    /**
     * 表单名称
     */
    @Desc("表单名称")
    private String formName;


    public static MdmForm copy(MdmForm source, MdmForm target) {
        if (target == null ) { target = new MdmForm();}
        if (source == null) { return target; }
        target.setId(source.getId());
        target.setFormCode(source.getFormCode());
        target.setFormName(source.getFormName());
        target.setSort(source.getSort());
        target.setCreateTime(source.getCreateTime());
        target.setCreateBy(source.getCreateBy());
        target.setUpdateTime(source.getUpdateTime());
        target.setUpdateBy(source.getUpdateBy());
        target.setRemark(source.getRemark());
        target.setVersion(source.getVersion());
        return target;
    }

    public static MdmForm copyIfNotNull(MdmForm source, MdmForm target) {
        if (target == null ) { target = new MdmForm();}
        if (source == null) { return target; }
        if (source.getId() != null) { target.setId(source.getId()); }
        if (source.getFormCode() != null) { target.setFormCode(source.getFormCode()); }
        if (source.getFormName() != null) { target.setFormName(source.getFormName()); }
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

