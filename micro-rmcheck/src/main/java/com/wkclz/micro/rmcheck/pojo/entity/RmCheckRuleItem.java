package com.wkclz.micro.rmcheck.pojo.entity;

import com.wkclz.core.annotation.Desc;
import com.wkclz.core.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;




/**
 * Description Create by shrimp-gen
 * @author wangkaicun
 * @table rm_check_rule_item (删除检查规则-检查项) 重新生成代码会覆盖
 */

@Data
@EqualsAndHashCode(callSuper = false)
public class RmCheckRuleItem extends BaseEntity {

    /**
     * 规则编码
     */
    @Desc("规则编码")
    private String ruleCode;

    /**
     * 被检查表名
     */
    @Desc("被检查表名")
    private String checkTableName;

    /**
     * 被检查字段名
     */
    @Desc("被检查字段名")
    private String checkColumnName;

    /**
     * 提示信息
     */
    @Desc("提示信息")
    private String noticeMessage;

    /**
     * 状态
     */
    @Desc("状态")
    private Integer enableFlag;


    public static RmCheckRuleItem copy(RmCheckRuleItem source, RmCheckRuleItem target) {
        if (target == null ) { target = new RmCheckRuleItem();}
        if (source == null) { return target; }
        target.setId(source.getId());
        target.setRuleCode(source.getRuleCode());
        target.setCheckTableName(source.getCheckTableName());
        target.setCheckColumnName(source.getCheckColumnName());
        target.setNoticeMessage(source.getNoticeMessage());
        target.setEnableFlag(source.getEnableFlag());
        target.setSort(source.getSort());
        target.setCreateTime(source.getCreateTime());
        target.setCreateBy(source.getCreateBy());
        target.setUpdateTime(source.getUpdateTime());
        target.setUpdateBy(source.getUpdateBy());
        target.setRemark(source.getRemark());
        target.setVersion(source.getVersion());
        return target;
    }

    public static RmCheckRuleItem copyIfNotNull(RmCheckRuleItem source, RmCheckRuleItem target) {
        if (target == null ) { target = new RmCheckRuleItem();}
        if (source == null) { return target; }
        if (source.getId() != null) { target.setId(source.getId()); }
        if (source.getRuleCode() != null) { target.setRuleCode(source.getRuleCode()); }
        if (source.getCheckTableName() != null) { target.setCheckTableName(source.getCheckTableName()); }
        if (source.getCheckColumnName() != null) { target.setCheckColumnName(source.getCheckColumnName()); }
        if (source.getNoticeMessage() != null) { target.setNoticeMessage(source.getNoticeMessage()); }
        if (source.getEnableFlag() != null) { target.setEnableFlag(source.getEnableFlag()); }
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

