package com.wkclz.micro.rmcheck.pojo.dto;

import com.wkclz.micro.rmcheck.pojo.entity.RmCheckRuleItem;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Description Create by shrimp-gen
 * @author wangkaicun
 * @table rm_check_rule_item (删除检查规则-检查项) 数据库实例扩展，代码重新生成不覆盖
 */

@Data
@EqualsAndHashCode(callSuper = false)
public class RmCheckRuleItemDto extends RmCheckRuleItem {

    private String tableSchema;

    private String checkTableComment;
    private String checkColumnComment;


    private Object checkValue;


    /**
     * entity 转 Dto
     * @param source
     * @return
     */
    public static RmCheckRuleItemDto copy(RmCheckRuleItem source) {
        RmCheckRuleItemDto target = new RmCheckRuleItemDto();
        RmCheckRuleItem.copy(source, target);
        return target;
    }
}

