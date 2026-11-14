package com.wkclz.micro.dag.mapper;

import com.wkclz.micro.dag.bean.entity.DagExecutionLog;
import com.wkclz.mybatis.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface DagExecutionLogMapper extends BaseMapper<DagExecutionLog> {

    /** 按节点查询执行记录（时间正序） */
    List<DagExecutionLog> listByNode(@Param("instanceCode") String instanceCode, @Param("nodeCode") String nodeCode);
}
