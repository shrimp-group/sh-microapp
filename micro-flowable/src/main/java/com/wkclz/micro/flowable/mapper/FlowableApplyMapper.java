package com.wkclz.micro.flowable.mapper;

import com.wkclz.micro.flowable.bean.entity.FlowableApply;
import com.wkclz.mybatis.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface FlowableApplyMapper extends BaseMapper<FlowableApply> {
    List<FlowableApply> getApplyPage(FlowableApply entity);
}
