package com.wkclz.micro.report.bean.entity;

import com.wkclz.core.annotation.FieldDesc;
import com.wkclz.core.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class ReportDefinitionHis extends BaseEntity {

    @FieldDesc("原数据ID")
    private Long dataId;

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

    public static ReportDefinitionHis copy(ReportDefinitionHis source, ReportDefinitionHis target) {
        if (target == null) { target = new ReportDefinitionHis(); }
        if (source == null) { return target; }
        target.setId(source.getId());
        target.setDataId(source.getDataId());
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

}
