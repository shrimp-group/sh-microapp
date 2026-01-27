package com.wkclz.micro.liteflow.dao;

import com.wkclz.micro.liteflow.pojo.entity.LiteflowScript;
import com.wkclz.mybatis.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * Description Create by shrimp-gen
 * @author wangkaicun
 * @table liteflow_script (liteflow-脚本) DAO 接口，代码重新生成不覆盖
 */

@Mapper
public interface LiteflowScriptMapper extends BaseMapper<LiteflowScript> {

    List<LiteflowScript> getLiteflowScriptList4Page(LiteflowScript entity);

}

