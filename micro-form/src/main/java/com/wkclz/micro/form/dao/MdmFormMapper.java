package com.wkclz.micro.form.dao;

import com.wkclz.micro.form.pojo.dto.MdmFormDto;
import com.wkclz.micro.form.pojo.entity.MdmForm;
import com.wkclz.mybatis.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * Description Create by shrimp-gen
 * @author wangkaicun
 * @table mdm_form (表单) DAO 接口，代码重新生成不覆盖
 */

@Mapper
public interface MdmFormMapper extends BaseMapper<MdmForm> {

    List<MdmForm> getFormOptions();

    List<MdmFormDto> getFormList(MdmFormDto fto);

    MdmFormDto getCustomFormInfo(@Param("formCode") String formCode);

    List<MdmFormDto> getForm4Cache();

}

