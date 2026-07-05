package com.wkclz.micro.material.mapper;

import com.wkclz.micro.material.bean.entity.MdmMaterial;
import com.wkclz.mybatis.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface MdmMaterialMapper extends BaseMapper<MdmMaterial> {

    List<MdmMaterial> getMaterialList4Page(MdmMaterial entity);

    List<MdmMaterial> getByGroupCodeWithChildren(@Param("groupCodes") List<String> groupCodes, @Param("tenantCode") String tenantCode);

    List<MdmMaterial> getHotMaterialList(MdmMaterial entity);

    List<MdmMaterial> getPickerMaterialList(MdmMaterial entity);
}
