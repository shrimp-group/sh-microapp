package com.wkclz.micro.fun.mapper;

import com.wkclz.micro.fun.bean.dto.FunFunctionDto;
import com.wkclz.micro.fun.bean.entity.FunFunction;
import com.wkclz.mybatis.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * Description Create by shrimp-gen
 * @author wangkaicun
 * @table fun_function (函数-函数体) Mapper 接口，代码重新生成不覆盖
 */

@Mapper
public interface FunFunctionMapper extends BaseMapper<FunFunction> {

    List<FunFunctionDto> getFunctionList(FunFunctionDto dto);

    List<FunFunctionDto> getFunctionOption(FunFunctionDto dto);

}

