package com.wkclz.micro.mask.pojo.entity;

import com.wkclz.core.annotation.Desc;
import com.wkclz.core.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;




/**
 * Description Create by shrimp-gen
 * @author wangkaicun
 * @table mdm_mask_rule (脱敏规则) 重新生成代码会覆盖
 */

@Data
@EqualsAndHashCode(callSuper = false)
public class MdmMaskRule extends BaseEntity {

    /**
     * 脱敏规则编码
     */
    @Desc("脱敏规则编码")
    private String maskRuleCode;

    /**
     * 脱敏规则名称
     */
    @Desc("脱敏规则名称")
    private String maskRuleName;

    /**
     * 请求方法
     */
    @Desc("请求方法")
    private String requestMethod;

    /**
     * 请求路径,支持AntPathMatcher
     */
    @Desc("请求路径,支持AntPathMatcher")
    private String requestUri;

    /**
     * 脱敏数据路径
     */
    @Desc("脱敏数据路径")
    private String maskJsonPath;

    /**
     * 脱敏正则
     */
    @Desc("脱敏正则")
    private String maskRuleRegular;

    /**
     * 脱敏函数
     */
    @Desc("脱敏函数")
    private String maskRuleScript;

    /**
     * 可用状态
     */
    @Desc("可用状态")
    private Integer enableFlag;

    /**
     * 示例值
     */
    @Desc("示例值")
    private String mockValue;


    public static MdmMaskRule copy(MdmMaskRule source, MdmMaskRule target) {
        if (target == null ) { target = new MdmMaskRule();}
        if (source == null) { return target; }
        target.setId(source.getId());
        target.setMaskRuleCode(source.getMaskRuleCode());
        target.setMaskRuleName(source.getMaskRuleName());
        target.setRequestMethod(source.getRequestMethod());
        target.setRequestUri(source.getRequestUri());
        target.setMaskJsonPath(source.getMaskJsonPath());
        target.setMaskRuleRegular(source.getMaskRuleRegular());
        target.setMaskRuleScript(source.getMaskRuleScript());
        target.setEnableFlag(source.getEnableFlag());
        target.setMockValue(source.getMockValue());
        target.setSort(source.getSort());
        target.setCreateTime(source.getCreateTime());
        target.setCreateBy(source.getCreateBy());
        target.setUpdateTime(source.getUpdateTime());
        target.setUpdateBy(source.getUpdateBy());
        target.setRemark(source.getRemark());
        target.setVersion(source.getVersion());
        return target;
    }

    public static MdmMaskRule copyIfNotNull(MdmMaskRule source, MdmMaskRule target) {
        if (target == null ) { target = new MdmMaskRule();}
        if (source == null) { return target; }
        if (source.getId() != null) { target.setId(source.getId()); }
        if (source.getMaskRuleCode() != null) { target.setMaskRuleCode(source.getMaskRuleCode()); }
        if (source.getMaskRuleName() != null) { target.setMaskRuleName(source.getMaskRuleName()); }
        if (source.getRequestMethod() != null) { target.setRequestMethod(source.getRequestMethod()); }
        if (source.getRequestUri() != null) { target.setRequestUri(source.getRequestUri()); }
        if (source.getMaskJsonPath() != null) { target.setMaskJsonPath(source.getMaskJsonPath()); }
        if (source.getMaskRuleRegular() != null) { target.setMaskRuleRegular(source.getMaskRuleRegular()); }
        if (source.getMaskRuleScript() != null) { target.setMaskRuleScript(source.getMaskRuleScript()); }
        if (source.getEnableFlag() != null) { target.setEnableFlag(source.getEnableFlag()); }
        if (source.getMockValue() != null) { target.setMockValue(source.getMockValue()); }
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

