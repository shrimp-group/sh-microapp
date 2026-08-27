package com.wkclz.micro.flowable.service;

import com.wkclz.core.base.PageData;
import com.wkclz.micro.flowable.bean.entity.FlowableApply;
import com.wkclz.micro.flowable.mapper.FlowableApplyMapper;
import com.wkclz.mybatis.helper.PageQuery;
import com.wkclz.mybatis.service.BaseService;
import org.springframework.stereotype.Service;

@Service
public class FlowableApplyService extends BaseService<FlowableApply, FlowableApplyMapper> {

    public PageData<FlowableApply> getApplyPage(FlowableApply entity) {
        return PageQuery.page(entity, mapper::getApplyPage);
    }
}
