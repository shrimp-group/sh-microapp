package com.wkclz.micro.material.mapper;

import com.wkclz.micro.material.bean.entity.MdmMaterialGroup;
import com.wkclz.mybatis.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface MdmMaterialGroupMapper extends BaseMapper<MdmMaterialGroup> {

    List<MdmMaterialGroup> getGroupTree(@Param("tenantCode") String tenantCode, @Param("userCode") String userCode);

    List<MdmMaterialGroup> getGroups4Cache();

    List<MdmMaterialGroup> getChildGroupCodes(@Param("parentCode") String parentCode, @Param("tenantCode") String tenantCode);

    List<MdmMaterialGroup> getPickerGroupTree(@Param("tenantCode") String tenantCode, @Param("userCode") String userCode);
}
