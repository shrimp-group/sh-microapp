package com.wkclz.micro.report.service;

import com.wkclz.core.base.PageData;
import com.wkclz.micro.report.bean.entity.ReportDefinitionHis;
import com.wkclz.micro.report.mapper.ReportDefinitionHisMapper;
import com.wkclz.mybatis.helper.PageQuery;
import com.wkclz.mybatis.service.BaseService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class ReportDefinitionHisService extends BaseService<ReportDefinitionHis, ReportDefinitionHisMapper> {

    /**
     * 历史版本分页查询
     */
    public PageData<ReportDefinitionHis> getHisPage(ReportDefinitionHis entity) {
        return PageQuery.page(entity, mapper::getHisList);
    }

    /**
     * 历史版本详情
     */
    public ReportDefinitionHis getHisDetail(Long id) {
        return selectById(id);
    }

}
