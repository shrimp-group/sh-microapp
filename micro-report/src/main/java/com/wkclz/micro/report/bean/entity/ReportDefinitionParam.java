package com.wkclz.micro.report.bean.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import com.wkclz.core.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class ReportDefinitionParam extends BaseEntity {

    @Schema(description = "报表编码")
    private String reportCode;

    @Schema(description = "字段编码")
    private String fieldCode;

    @Schema(description = "字段名称")
    private String fieldName;

    @Schema(description = "字段类型")
    private String fieldType;

    @Schema(description = "表单类型")
    private String fieldFormType;

    @Schema(description = "输入提示")
    private String placeholder;

    @Schema(description = "是否必填")
    private Integer required;

    @Schema(description = "校验JS脚本")
    private String validateScript;

    @Schema(description = "字典类型")
    private String dictType;

    @Schema(description = "列表宽度")
    private Integer width;

    public static ReportDefinitionParam copy(ReportDefinitionParam source, ReportDefinitionParam target) {
        if (target == null) { target = new ReportDefinitionParam(); }
        if (source == null) { return target; }
        target.setId(source.getId());
        target.setReportCode(source.getReportCode());
        target.setFieldCode(source.getFieldCode());
        target.setFieldName(source.getFieldName());
        target.setFieldType(source.getFieldType());
        target.setFieldFormType(source.getFieldFormType());
        target.setPlaceholder(source.getPlaceholder());
        target.setRequired(source.getRequired());
        target.setValidateScript(source.getValidateScript());
        target.setDictType(source.getDictType());
        target.setWidth(source.getWidth());
        target.setSort(source.getSort());
        target.setCreateTime(source.getCreateTime());
        target.setCreateBy(source.getCreateBy());
        target.setUpdateTime(source.getUpdateTime());
        target.setUpdateBy(source.getUpdateBy());
        target.setRemark(source.getRemark());
        target.setVersion(source.getVersion());
        return target;
    }

    public static ReportDefinitionParam copyIfNotNull(ReportDefinitionParam source, ReportDefinitionParam target) {
        if (target == null) { target = new ReportDefinitionParam(); }
        if (source == null) { return target; }
        if (source.getId() != null) { target.setId(source.getId()); }
        if (source.getReportCode() != null) { target.setReportCode(source.getReportCode()); }
        if (source.getFieldCode() != null) { target.setFieldCode(source.getFieldCode()); }
        if (source.getFieldName() != null) { target.setFieldName(source.getFieldName()); }
        if (source.getFieldType() != null) { target.setFieldType(source.getFieldType()); }
        if (source.getFieldFormType() != null) { target.setFieldFormType(source.getFieldFormType()); }
        if (source.getPlaceholder() != null) { target.setPlaceholder(source.getPlaceholder()); }
        if (source.getRequired() != null) { target.setRequired(source.getRequired()); }
        if (source.getValidateScript() != null) { target.setValidateScript(source.getValidateScript()); }
        if (source.getDictType() != null) { target.setDictType(source.getDictType()); }
        if (source.getWidth() != null) { target.setWidth(source.getWidth()); }
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
