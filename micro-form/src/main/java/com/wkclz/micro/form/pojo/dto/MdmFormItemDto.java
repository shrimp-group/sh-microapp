package com.wkclz.micro.form.pojo.dto;

import com.wkclz.micro.form.pojo.entity.MdmFormItem;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Description Create by shrimp-gen
 * @author wangkaicun
 * @table mdm_form_item (表单-输入项) 数据库实例扩展，代码重新生成不覆盖
 */

@Data
@EqualsAndHashCode(callSuper = false)
public class MdmFormItemDto extends MdmFormItem {




    /**
     * entity 转 Dto
     * @param source
     * @return
     */
    public static MdmFormItemDto copy(MdmFormItem source) {
        MdmFormItemDto target = new MdmFormItemDto();
        MdmFormItem.copy(source, target);
        return target;
    }
}

