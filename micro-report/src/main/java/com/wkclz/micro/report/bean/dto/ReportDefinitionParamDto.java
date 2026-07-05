package com.wkclz.micro.report.bean.dto;

import com.wkclz.micro.report.bean.entity.ReportDefinitionParam;
import com.wkclz.tool.utils.BeanUtil;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class ReportDefinitionParamDto extends ReportDefinitionParam {

    private String reportScript;
    private Integer reportScriptAutocamel;

    public static ReportDefinitionParamDto copy(ReportDefinitionParam source) {
        return BeanUtil.cp(source, ReportDefinitionParamDto.class);
    }

}
