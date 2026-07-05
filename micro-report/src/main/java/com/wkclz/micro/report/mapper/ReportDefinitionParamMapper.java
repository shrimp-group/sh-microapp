package com.wkclz.micro.report.mapper;

import com.wkclz.micro.report.bean.entity.ReportDefinitionParam;
import com.wkclz.mybatis.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface ReportDefinitionParamMapper extends BaseMapper<ReportDefinitionParam> {

    List<ReportDefinitionParam> getParamList(ReportDefinitionParam entity);

    void updateReportCodeBatch(String oldCode, String newCode);

    void deleteByReportCode(String reportCode);

}
