package com.wkclz.micro.fun.dao;

import com.wkclz.micro.fun.pojo.dto.FunFunctionDto;
import com.wkclz.micro.fun.pojo.entity.FunFunction;
import com.wkclz.mybatis.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * Description Create by shrimp-gen
 * @author wangkaicun
 * @table fun_function (函数-函数体) DAO 接口，代码重新生成不覆盖
 */

@Mapper
public interface FunFunctionMapper extends BaseMapper<FunFunction> {

    List<FunFunctionDto> getFunctionList(FunFunctionDto dto);

    List<FunFunctionDto> getFunctionOption(FunFunctionDto dto);

}

