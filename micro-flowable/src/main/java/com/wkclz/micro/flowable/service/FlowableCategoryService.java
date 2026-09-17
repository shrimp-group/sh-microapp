package com.wkclz.micro.flowable.service;

import com.wkclz.core.base.PageData;
import com.wkclz.micro.flowable.bean.entity.FlowableCategory;
import com.wkclz.micro.flowable.mapper.FlowableCategoryMapper;
import com.wkclz.mybatis.helper.PageQuery;
import com.wkclz.mybatis.service.BaseService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FlowableCategoryService extends BaseService<FlowableCategory, FlowableCategoryMapper> {

    public PageData<FlowableCategory> getCategoryPage(FlowableCategory entity) {
        return PageQuery.page(entity, mapper::getCategoryPage);
    }

    public List<FlowableCategory> getCategoryList(FlowableCategory entity) {
        return mapper.getCategoryList(entity);
    }
}
