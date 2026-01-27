package com.wkclz.micro.form.dao;

import com.wkclz.micro.form.pojo.entity.MdmFormItem;
import com.wkclz.mybatis.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * Description Create by shrimp-gen
 * @author wangkaicun
 * @table mdm_form_item (表单-输入项) DAO 接口，代码重新生成不覆盖
 */

@Mapper
public interface MdmFormItemMapper extends BaseMapper<MdmFormItem> {

    List<MdmFormItem> getCustomFormItemList(@Param("formCode") String formCode);


    List<MdmFormItem> getFormItem4Cache();


}

