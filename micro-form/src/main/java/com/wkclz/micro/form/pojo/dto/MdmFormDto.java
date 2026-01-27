package com.wkclz.micro.form.pojo.dto;

import com.wkclz.micro.form.pojo.entity.MdmForm;
import com.wkclz.micro.form.pojo.entity.MdmFormItem;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * Description Create by shrimp-gen
 * @author wangkaicun
 * @table mdm_form (表单) 数据库实例扩展，代码重新生成不覆盖
 */

@Data
@EqualsAndHashCode(callSuper = false)
public class MdmFormDto extends MdmForm {

    private Integer itemCount;

    private List<MdmFormItem> items;

    /**
     * entity 转 Dto
     * @param source
     * @return
     */
    public static MdmFormDto copy(MdmForm source) {
        MdmFormDto target = new MdmFormDto();
        MdmForm.copy(source, target);
        return target;
    }
}

