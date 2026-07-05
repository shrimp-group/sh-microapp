package com.wkclz.micro.report.bean.dto;

import com.wkclz.micro.report.bean.entity.ReportDefinitionResult;
import com.wkclz.tool.utils.BeanUtil;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class ReportDefinitionResultDto extends ReportDefinitionResult {

    private String reportScript;
    private Integer reportScriptAutocamel;

    public static ReportDefinitionResultDto copy(ReportDefinitionResult source) {
        return BeanUtil.cp(source, ReportDefinitionResultDto.class);
    }

}
