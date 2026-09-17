package com.wkclz.micro.flowable.mapper;

import com.wkclz.micro.flowable.bean.entity.FlowableCategory;
import com.wkclz.mybatis.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface FlowableCategoryMapper extends BaseMapper<FlowableCategory> {
    List<FlowableCategory> getCategoryPage(FlowableCategory entity);

    List<FlowableCategory> getCategoryList(FlowableCategory entity);
}
