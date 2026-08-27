package com.wkclz.micro.flowable.service;

import com.wkclz.core.base.PageData;
import com.wkclz.micro.flowable.bean.entity.FlowableErrorLog;
import com.wkclz.micro.flowable.mapper.FlowableErrorLogMapper;
import com.wkclz.mybatis.helper.PageQuery;
import com.wkclz.mybatis.service.BaseService;
import org.springframework.stereotype.Service;

@Service
public class FlowableErrorLogService extends BaseService<FlowableErrorLog, FlowableErrorLogMapper> {

    public PageData<FlowableErrorLog> getErrorLogPage(FlowableErrorLog entity) {
        return PageQuery.page(entity, mapper::getErrorLogPage);
    }
}
