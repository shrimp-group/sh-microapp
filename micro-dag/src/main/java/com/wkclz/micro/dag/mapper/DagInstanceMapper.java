package com.wkclz.micro.dag.mapper;

import com.wkclz.micro.dag.bean.entity.DagInstance;
import com.wkclz.mybatis.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface DagInstanceMapper extends BaseMapper<DagInstance> {

    /** 查询运行中的实例 id（调度扫描范围） */
    List<DagInstance> listRunning();
}
