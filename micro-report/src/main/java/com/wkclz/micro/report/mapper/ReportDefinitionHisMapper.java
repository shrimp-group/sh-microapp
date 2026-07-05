package com.wkclz.micro.report.mapper;

import com.wkclz.micro.report.bean.entity.ReportDefinitionHis;
import com.wkclz.mybatis.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface ReportDefinitionHisMapper extends BaseMapper<ReportDefinitionHis> {

    List<ReportDefinitionHis> getHisList(ReportDefinitionHis entity);

}
