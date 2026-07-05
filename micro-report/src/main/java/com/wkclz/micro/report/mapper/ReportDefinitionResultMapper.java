package com.wkclz.micro.report.mapper;

import com.wkclz.micro.report.bean.entity.ReportDefinitionResult;
import com.wkclz.mybatis.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface ReportDefinitionResultMapper extends BaseMapper<ReportDefinitionResult> {

    List<ReportDefinitionResult> getResultList(ReportDefinitionResult entity);

    void updateReportCodeBatch(String oldCode, String newCode);

    void deleteByReportCode(String reportCode);

}
