package com.wkclz.micro.flowable.mapper;

import com.wkclz.micro.flowable.bean.entity.FlowableErrorLog;
import com.wkclz.mybatis.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface FlowableErrorLogMapper extends BaseMapper<FlowableErrorLog> {
    List<FlowableErrorLog> getErrorLogPage(FlowableErrorLog entity);
}
