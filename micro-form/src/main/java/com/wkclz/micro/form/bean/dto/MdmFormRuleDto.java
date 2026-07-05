package com.wkclz.micro.form.bean.dto;

import com.wkclz.micro.form.bean.entity.MdmFormRule;
import com.wkclz.tool.utils.BeanUtil;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * Description Create by shrimp-gen
 * @author wangkaicun
 * @table mdm_form_rule (表单校验规则) 数据库实例扩展，代码重新生成不覆盖
 */

@Data
@EqualsAndHashCode(callSuper = false)
public class MdmFormRuleDto extends MdmFormRule {

    // 规则数量
    private Integer itemCount;

    private List<MdmFormRuleFieldDto> fields;

    /**
     * entity 转 Dto
     * @param source
     * @return
     */
    public static MdmFormRuleDto copy(MdmFormRule source) {
        return BeanUtil.cp(source, MdmFormRuleDto.class);
    }
}
