package com.wkclz.micro.report.mapper;

import com.wkclz.micro.report.bean.dto.ReportDefinitionDto;
import com.wkclz.micro.report.bean.entity.ReportDefinition;
import com.wkclz.mybatis.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface ReportDefinitionMapper extends BaseMapper<ReportDefinition> {

    List<ReportDefinitionDto> getDefinitionList(ReportDefinition entity);

    List<ReportDefinition> definitions4Cache();

    void updateReportCodeBatch(String oldCode, String newCode);

}
