package com.wkclz.micro.report.bean.entity;

import com.wkclz.core.annotation.FieldDesc;
import com.wkclz.core.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class ReportDefinitionResult extends BaseEntity {

    @FieldDesc("报表编码")
    private String reportCode;

    @FieldDesc("字段编码")
    private String fieldCode;

    @FieldDesc("字段名称")
    private String fieldName;

    @FieldDesc("字段类型")
    private String fieldType;

    @FieldDesc("展示类型")
    private String fieldFormType;

    @FieldDesc("列宽")
    private Integer width;

    public static ReportDefinitionResult copy(ReportDefinitionResult source, ReportDefinitionResult target) {
        if (target == null) { target = new ReportDefinitionResult(); }
        if (source == null) { return target; }
        target.setId(source.getId());
        target.setReportCode(source.getReportCode());
        target.setFieldCode(source.getFieldCode());
        target.setFieldName(source.getFieldName());
        target.setFieldType(source.getFieldType());
        target.setFieldFormType(source.getFieldFormType());
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

    public static ReportDefinitionResult copyIfNotNull(ReportDefinitionResult source, ReportDefinitionResult target) {
        if (target == null) { target = new ReportDefinitionResult(); }
        if (source == null) { return target; }
        if (source.getId() != null) { target.setId(source.getId()); }
        if (source.getReportCode() != null) { target.setReportCode(source.getReportCode()); }
        if (source.getFieldCode() != null) { target.setFieldCode(source.getFieldCode()); }
        if (source.getFieldName() != null) { target.setFieldName(source.getFieldName()); }
        if (source.getFieldType() != null) { target.setFieldType(source.getFieldType()); }
        if (source.getFieldFormType() != null) { target.setFieldFormType(source.getFieldFormType()); }
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
