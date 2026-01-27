package com.wkclz.micro.liteflow.pojo.entity;

import com.wkclz.core.annotation.Desc;
import com.wkclz.core.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;




/**
 * Description Create by shrimp-gen
 * @author wangkaicun
 * @table liteflow_script (liteflow-脚本) 重新生成代码会覆盖
 */

@Data
@EqualsAndHashCode(callSuper = false)
public class LiteflowScript extends BaseEntity {

    /**
     * 脚本ID
     */
    @Desc("脚本ID")
    private String scriptId;

    /**
     * 脚本名称
     */
    @Desc("脚本名称")
    private String scriptName;

    /**
     * 脚本数据
     */
    @Desc("脚本数据")
    private String scriptData;

    /**
     * 脚本类型
     */
    @Desc("脚本类型")
    private String scriptType;

    /**
     * 脚本语言
     */
    @Desc("脚本语言")
    private String scriptLanguage;

    /**
     * 可用状态
     */
    @Desc("可用状态")
    private Integer enable;


    public static LiteflowScript copy(LiteflowScript source, LiteflowScript target) {
        if (target == null ) { target = new LiteflowScript();}
        if (source == null) { return target; }
        target.setId(source.getId());
        target.setScriptId(source.getScriptId());
        target.setScriptName(source.getScriptName());
        target.setScriptData(source.getScriptData());
        target.setScriptType(source.getScriptType());
        target.setScriptLanguage(source.getScriptLanguage());
        target.setEnable(source.getEnable());
        target.setSort(source.getSort());
        target.setCreateTime(source.getCreateTime());
        target.setCreateBy(source.getCreateBy());
        target.setUpdateTime(source.getUpdateTime());
        target.setUpdateBy(source.getUpdateBy());
        target.setRemark(source.getRemark());
        target.setVersion(source.getVersion());
        return target;
    }

    public static LiteflowScript copyIfNotNull(LiteflowScript source, LiteflowScript target) {
        if (target == null ) { target = new LiteflowScript();}
        if (source == null) { return target; }
        if (source.getId() != null) { target.setId(source.getId()); }
        if (source.getScriptId() != null) { target.setScriptId(source.getScriptId()); }
        if (source.getScriptName() != null) { target.setScriptName(source.getScriptName()); }
        if (source.getScriptData() != null) { target.setScriptData(source.getScriptData()); }
        if (source.getScriptType() != null) { target.setScriptType(source.getScriptType()); }
        if (source.getScriptLanguage() != null) { target.setScriptLanguage(source.getScriptLanguage()); }
        if (source.getEnable() != null) { target.setEnable(source.getEnable()); }
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

