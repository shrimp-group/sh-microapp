package com.wkclz.micro.liteflow.mapper;

import com.wkclz.micro.liteflow.bean.entity.LiteflowChain;
import com.wkclz.mybatis.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * Description Create by shrimp-gen
 * @author wangkaicun
 * @table liteflow_chain (liteflow-规则) Mapper 接口，代码重新生成不覆盖
 */

@Mapper
public interface LiteflowChainMapper extends BaseMapper<LiteflowChain> {

    List<LiteflowChain> getLiteflowChainList4Page(LiteflowChain entity);

}

