package com.wkclz.micro.dict.dao;

import com.wkclz.micro.dict.pojo.dto.MdmDictDto;
import com.wkclz.micro.dict.pojo.entity.MdmDict;
import com.wkclz.mybatis.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * Description Create by shrimp-gen
 * @author wangkaicun
 * @table mdm_dict (字典) DAO 接口，代码重新生成不覆盖
 */

@Mapper
public interface MdmDictMapper extends BaseMapper<MdmDict> {

    List<MdmDict> getDictList(MdmDict entity);

    List<MdmDict> dicts4Cache();

    List<MdmDictDto> dicts4Copy(MdmDictDto dto);

    List<MdmDictDto> dicts4Update(MdmDictDto dto);

    List<MdmDict> dictOptions();

}

