package com.wkclz.micro.flowable.service;

import com.wkclz.core.base.PageData;
import com.wkclz.micro.flowable.bean.entity.MdmFlowableProcessDesign;
import com.wkclz.micro.flowable.mapper.MdmFlowableProcessDesignMapper;
import com.wkclz.mybatis.helper.PageQuery;
import com.wkclz.mybatis.service.BaseService;
import org.springframework.stereotype.Service;

@Service
public class MdmFlowableProcessDesignService extends BaseService<MdmFlowableProcessDesign, MdmFlowableProcessDesignMapper> {

    public PageData<MdmFlowableProcessDesign> getDesignPage(MdmFlowableProcessDesign entity) {
        return PageQuery.page(entity, mapper::getDesignPage);
    }
}
