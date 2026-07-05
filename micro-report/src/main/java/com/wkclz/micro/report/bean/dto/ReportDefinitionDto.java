package com.wkclz.micro.report.bean.dto;

import com.wkclz.micro.report.bean.entity.ReportDefinition;
import com.wkclz.micro.report.bean.entity.ReportDefinitionParam;
import com.wkclz.micro.report.bean.entity.ReportDefinitionResult;
import com.wkclz.tool.utils.BeanUtil;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
public class ReportDefinitionDto extends ReportDefinition {

    private Long paramCount;
    private Long resultCount;
    private List<ReportDefinitionParam> params;
    private List<ReportDefinitionResult> results;

    public static ReportDefinitionDto copy(ReportDefinition source) {
        return BeanUtil.cp(source, ReportDefinitionDto.class);
    }

}
