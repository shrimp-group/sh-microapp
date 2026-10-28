package com.wkclz.micro.flowable.mapper;

import com.wkclz.micro.flowable.bean.entity.FlowableNodeConfig;
import com.wkclz.mybatis.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface FlowableNodeConfigMapper extends BaseMapper<FlowableNodeConfig> {
    List<FlowableNodeConfig> getNodeConfigList(FlowableNodeConfig entity);
}
