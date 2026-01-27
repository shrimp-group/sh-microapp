package com.wkclz.micro.mask.pojo.dto;

import com.wkclz.micro.mask.pojo.entity.MdmMaskRule;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Description Create by shrimp-gen
 * @author wangkaicun
 * @table mdm_mask_rule (脱敏规则) 数据库实例扩展，代码重新生成不覆盖
 */

@Data
@EqualsAndHashCode(callSuper = false)
public class MdmMaskRuleDto extends MdmMaskRule {


    private String maskValue;
    private String maskType;

    /**
     * entity 转 Dto
     * @param source
     * @return
     */
    public static MdmMaskRuleDto copy(MdmMaskRule source) {
        MdmMaskRuleDto target = new MdmMaskRuleDto();
        MdmMaskRule.copy(source, target);
        return target;
    }
}

