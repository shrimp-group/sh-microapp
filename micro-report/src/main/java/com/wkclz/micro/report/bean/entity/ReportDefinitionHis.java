package com.wkclz.micro.report.bean.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import com.wkclz.core.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class ReportDefinitionHis extends BaseEntity {

    @Schema(description = "原数据ID")
    private Long dataId;

    @Schema(description = "报表编码")
    private String reportCode;

    @Schema(description = "报表名称")
    private String reportName;

    @Schema(description = "返回值类型：OBJECT/LIST/PAGE")
    private String resultType;

    @Schema(description = "启用状态")
    private Integer enableFlag;

    @Schema(description = "SQL查询脚本")
    private String reportScript;

    @Schema(description = "Count脚本开关")
    private Integer reportScriptCountSwitch;

    @Schema(description = "自定义COUNT SQL脚本")
    private String reportScriptCount;

    @Schema(description = "自动驼峰转换")
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
