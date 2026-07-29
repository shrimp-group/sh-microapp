package com.wkclz.micro.flowable.service;

import com.wkclz.core.base.PageData;
import com.wkclz.micro.flowable.bean.entity.MdmFlowableErrorLog;
import com.wkclz.micro.flowable.mapper.MdmFlowableErrorLogMapper;
import com.wkclz.mybatis.helper.PageQuery;
import com.wkclz.mybatis.service.BaseService;
import org.springframework.stereotype.Service;

@Service
public class MdmFlowableErrorLogService extends BaseService<MdmFlowableErrorLog, MdmFlowableErrorLogMapper> {

    public PageData<MdmFlowableErrorLog> getErrorLogPage(MdmFlowableErrorLog entity) {
        return PageQuery.page(entity, mapper::getErrorLogPage);
    }
}
