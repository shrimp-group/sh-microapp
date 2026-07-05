package com.wkclz.micro.material.mapper;

import com.wkclz.micro.material.bean.entity.MdmMaterialVersion;
import com.wkclz.mybatis.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface MdmMaterialVersionMapper extends BaseMapper<MdmMaterialVersion> {

    List<MdmMaterialVersion> getByMaterialCode(@Param("materialCode") String materialCode, @Param("tenantCode") String tenantCode);

    Integer deleteOldestVersions(@Param("materialCode") String materialCode, @Param("tenantCode") String tenantCode, @Param("keepCount") int keepCount);
}
