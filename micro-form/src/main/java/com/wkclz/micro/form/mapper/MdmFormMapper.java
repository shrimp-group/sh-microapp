package com.wkclz.micro.form.mapper;

import com.wkclz.micro.form.bean.dto.MdmFormDto;
import com.wkclz.mybatis.mapper.BaseMapper;
import com.wkclz.micro.form.bean.entity.MdmForm;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * Description Create by sh-generator
 * @author shrimp
 * @table mdm_form (表单) Mapper 接口，代码重新生成不覆盖
 */

@Mapper
public interface MdmFormMapper extends BaseMapper<MdmForm> {


    List<MdmForm> getFormOptions();

    List<MdmFormDto> getFormList(MdmFormDto fto);

    MdmFormDto getCustomFormInfo(@Param("formCode") String formCode);

    List<MdmFormDto> getForm4Cache();


}

