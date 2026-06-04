package com.wkclz.micro.fileos.mapper;

import com.wkclz.micro.fileos.bean.entity.MdmFileosBucket;
import com.wkclz.mybatis.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface MdmFileosBucketMapper extends BaseMapper<MdmFileosBucket> {

    List<MdmFileosBucket> getBucketList(MdmFileosBucket entity);

    List<MdmFileosBucket> getBucketOptions(MdmFileosBucket entity);

}
