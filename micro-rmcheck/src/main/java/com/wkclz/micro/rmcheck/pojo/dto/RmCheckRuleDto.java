package com.wkclz.micro.rmcheck.pojo.dto;

import com.wkclz.micro.rmcheck.pojo.entity.RmCheckRule;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Description Create by shrimp-gen
 * @author wangkaicun
 * @table rm_check_rule (删除检查规则) 数据库实例扩展，代码重新生成不覆盖
 */

@Data
@EqualsAndHashCode(callSuper = false)
public class RmCheckRuleDto extends RmCheckRule {


    private String tableSchema;
    private Integer itemCount;

    private String tableComment;
    private String columnComment;


    /**
     * entity 转 Dto
     * @param source
     * @return
     */
    public static RmCheckRuleDto copy(RmCheckRule source) {
        RmCheckRuleDto target = new RmCheckRuleDto();
        RmCheckRule.copy(source, target);
        return target;
    }
}

