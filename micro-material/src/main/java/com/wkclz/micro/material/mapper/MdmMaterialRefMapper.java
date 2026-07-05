package com.wkclz.micro.material.mapper;

import com.wkclz.micro.material.bean.entity.MdmMaterialRef;
import com.wkclz.mybatis.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface MdmMaterialRefMapper extends BaseMapper<MdmMaterialRef> {

    List<MdmMaterialRef> getByMaterialCode(@Param("materialCode") String materialCode, @Param("tenantCode") String tenantCode);

    Long countByMaterialCode(@Param("materialCode") String materialCode, @Param("tenantCode") String tenantCode);

    Integer deleteByBiz(@Param("materialCode") String materialCode, @Param("bizType") String bizType, @Param("bizCode") String bizCode, @Param("tenantCode") String tenantCode);
}
