package com.wkclz.micro.material.mapper;

import com.wkclz.micro.material.bean.entity.MdmMaterialTransferLog;
import com.wkclz.mybatis.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface MdmMaterialTransferLogMapper extends BaseMapper<MdmMaterialTransferLog> {

    List<MdmMaterialTransferLog> getByMaterialCode(@Param("materialCode") String materialCode, @Param("tenantCode") String tenantCode);
}
