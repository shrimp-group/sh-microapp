package com.wkclz.micro.file.dao;

import com.wkclz.micro.file.pojo.entity.MdmFsBucket;
import com.wkclz.mybatis.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * Description Create by shrimp-gen
 * @author wangkaicun
 * @table mdm_fs_bucket (Bucket管理) DAO 接口，代码重新生成不覆盖
 */

@Mapper
public interface MdmFsBucketMapper extends BaseMapper<MdmFsBucket> {

    List<MdmFsBucket> getBucketList(MdmFsBucket entity);

    List<MdmFsBucket> getBucketOptions(MdmFsBucket entity);


}

