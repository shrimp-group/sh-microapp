package com.wkclz.micro.dag.mapper;

import com.wkclz.micro.dag.bean.entity.DagNode;
import com.wkclz.mybatis.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface DagNodeMapper extends BaseMapper<DagNode> {
}
