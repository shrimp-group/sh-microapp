package com.wkclz.micro.flowable.service;

import com.wkclz.core.base.PageData;
import com.wkclz.micro.flowable.bean.entity.MdmFlowableApply;
import com.wkclz.micro.flowable.mapper.MdmFlowableApplyMapper;
import com.wkclz.mybatis.helper.PageQuery;
import com.wkclz.mybatis.service.BaseService;
import org.springframework.stereotype.Service;

@Service
public class MdmFlowableApplyService extends BaseService<MdmFlowableApply, MdmFlowableApplyMapper> {

    public PageData<MdmFlowableApply> getApplyPage(MdmFlowableApply entity) {
        return PageQuery.page(entity, mapper::getApplyPage);
    }
}
