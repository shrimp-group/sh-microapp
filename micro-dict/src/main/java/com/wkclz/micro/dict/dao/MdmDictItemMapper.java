package com.wkclz.micro.dict.dao;

import com.wkclz.micro.dict.pojo.dto.MdmDictItemDto;
import com.wkclz.micro.dict.pojo.entity.MdmDictItem;
import com.wkclz.mybatis.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * Description Create by shrimp-gen
 * @author wangkaicun
 * @table mdm_dict_item (字典内容) DAO 接口，代码重新生成不覆盖
 */

@Mapper
public interface MdmDictItemMapper extends BaseMapper<MdmDictItem> {

    List<MdmDictItem> getDictItemList(@Param("dictType") String dictType);

    List<MdmDictItemDto> getAllDictItem();

    List<MdmDictItem> getDictItemsByDictTypes(@Param("dictTypes") List<String> dictTypes);

    List<MdmDictItem> dictItems4Cache();

    List<MdmDictItem> dictItems4Copy(MdmDictItemDto dto);

    List<MdmDictItem> dictItems4Update(MdmDictItemDto dto);


}

