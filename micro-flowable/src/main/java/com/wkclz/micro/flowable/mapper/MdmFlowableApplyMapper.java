package com.wkclz.micro.flowable.mapper;

import com.wkclz.micro.flowable.bean.entity.MdmFlowableApply;
import com.wkclz.mybatis.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface MdmFlowableApplyMapper extends BaseMapper<MdmFlowableApply> {
    List<MdmFlowableApply> getApplyPage(MdmFlowableApply entity);
}
