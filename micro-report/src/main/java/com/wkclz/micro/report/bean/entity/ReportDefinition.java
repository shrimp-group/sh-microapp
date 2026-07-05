package com.wkclz.micro.report.bean.entity;

import com.wkclz.core.annotation.FieldDesc;
import com.wkclz.core.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class ReportDefinition extends BaseEntity {

    @FieldDesc("报表编码")
    private String reportCode;

    @FieldDesc("报表名称")
    private String reportName;

    @FieldDesc("返回值类型：OBJECT/LIST/PAGE")
    private String resultType;

    @FieldDesc("启用状态")
    private Integer enableFlag;

    @FieldDesc("SQL查询脚本")
    private String reportScript;

    @FieldDesc("Count脚本开关")
    private Integer reportScriptCountSwitch;

    @FieldDesc("自定义COUNT SQL脚本")
    private String reportScriptCount;

    @FieldDesc("自动驼峰转换")
    private Integer reportScriptAutocamel;

    public static ReportDefinition copy(ReportDefinition source, ReportDefinition target) {
        if (target == null) { target = new ReportDefinition(); }
        if (source == null) { return target; }
        target.setId(source.getId());
        target.setReportCode(source.getReportCode());
        target.setReportName(source.getReportName());
        target.setResultType(source.getResultType());
        target.setEnableFlag(source.getEnableFlag());
        target.setReportScript(source.getReportScript());
        target.setReportScriptCountSwitch(source.getReportScriptCountSwitch());
        target.setReportScriptCount(source.getReportScriptCount());
        target.setReportScriptAutocamel(source.getReportScriptAutocamel());
        target.setSort(source.getSort());
        target.setCreateTime(source.getCreateTime());
        target.setCreateBy(source.getCreateBy());
        target.setUpdateTime(source.getUpdateTime());
        target.setUpdateBy(source.getUpdateBy());
        target.setRemark(source.getRemark());
        target.setVersion(source.getVersion());
        return target;
    }

    public static ReportDefinition copyIfNotNull(ReportDefinition source, ReportDefinition target) {
        if (target == null) { target = new ReportDefinition(); }
        if (source == null) { return target; }
        if (source.getId() != null) { target.setId(source.getId()); }
        if (source.getReportCode() != null) { target.setReportCode(source.getReportCode()); }
        if (source.getReportName() != null) { target.setReportName(source.getReportName()); }
        if (source.getResultType() != null) { target.setResultType(source.getResultType()); }
        if (source.getEnableFlag() != null) { target.setEnableFlag(source.getEnableFlag()); }
        if (source.getReportScript() != null) { target.setReportScript(source.getReportScript()); }
        if (source.getReportScriptCountSwitch() != null) { target.setReportScriptCountSwitch(source.getReportScriptCountSwitch()); }
        if (source.getReportScriptCount() != null) { target.setReportScriptCount(source.getReportScriptCount()); }
        if (source.getReportScriptAutocamel() != null) { target.setReportScriptAutocamel(source.getReportScriptAutocamel()); }
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
