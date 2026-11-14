package com.wkclz.micro.dag.mapper;

import com.wkclz.micro.dag.bean.entity.DagDefinition;
import com.wkclz.mybatis.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface DagDefinitionMapper extends BaseMapper<DagDefinition> {
}
