package com.wkclz.micro.fun.dao;

import com.wkclz.micro.fun.pojo.entity.FunCategory;
import com.wkclz.mybatis.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * Description Create by shrimp-gen
 * @author wangkaicun
 * @table fun_category (函数-分类) DAO 接口，代码重新生成不覆盖
 */

@Mapper
public interface FunCategoryMapper extends BaseMapper<FunCategory> {

    List<FunCategory> getFunCategoryList(FunCategory entity);

    List<FunCategory> getFunCategoryOptions(FunCategory entity);

}

