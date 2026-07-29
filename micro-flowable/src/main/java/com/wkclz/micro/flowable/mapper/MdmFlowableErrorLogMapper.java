package com.wkclz.micro.flowable.mapper;

import com.wkclz.micro.flowable.bean.entity.MdmFlowableErrorLog;
import com.wkclz.mybatis.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface MdmFlowableErrorLogMapper extends BaseMapper<MdmFlowableErrorLog> {
    List<MdmFlowableErrorLog> getErrorLogPage(MdmFlowableErrorLog entity);
}
