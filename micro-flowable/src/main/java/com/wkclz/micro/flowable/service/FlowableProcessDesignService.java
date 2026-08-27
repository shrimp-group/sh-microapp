package com.wkclz.micro.flowable.service;

import com.wkclz.core.base.PageData;
import com.wkclz.micro.flowable.bean.entity.FlowableProcessDesign;
import com.wkclz.micro.flowable.mapper.FlowableProcessDesignMapper;
import com.wkclz.mybatis.helper.PageQuery;
import com.wkclz.mybatis.service.BaseService;
import org.springframework.stereotype.Service;

@Service
public class FlowableProcessDesignService extends BaseService<FlowableProcessDesign, FlowableProcessDesignMapper> {

    public PageData<FlowableProcessDesign> getDesignPage(FlowableProcessDesign entity) {
        return PageQuery.page(entity, mapper::getDesignPage);
    }
}
