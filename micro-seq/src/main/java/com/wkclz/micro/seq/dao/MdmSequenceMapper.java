package com.wkclz.micro.seq.dao;

import com.wkclz.micro.seq.pojo.entity.MdmSequence;
import com.wkclz.mybatis.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * Description Create by shrimp-gen
 * @author wangkaicun
 * @table mdm_sequence (序列生成) DAO 接口，代码重新生成不覆盖
 */

@Mapper
public interface MdmSequenceMapper extends BaseMapper<MdmSequence> {

    List<MdmSequence> getSequenceList(MdmSequence entity);

    MdmSequence getSequenceInfo(@Param("prefix") String prefix);

    Integer insertSequenceInfo(MdmSequence entity);

    Integer updateSequenceInfo(MdmSequence entity);

}

