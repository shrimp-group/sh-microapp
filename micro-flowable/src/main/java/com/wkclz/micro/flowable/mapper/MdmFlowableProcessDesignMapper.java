package com.wkclz.micro.flowable.mapper;

import com.wkclz.micro.flowable.bean.entity.MdmFlowableProcessDesign;
import com.wkclz.mybatis.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface MdmFlowableProcessDesignMapper extends BaseMapper<MdmFlowableProcessDesign> {
    List<MdmFlowableProcessDesign> getDesignPage(MdmFlowableProcessDesign entity);
}
