package com.wkclz.micro.file.mapper;

import com.wkclz.micro.file.bean.entity.MdmFileBucket;
import com.wkclz.mybatis.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * Description Create by sh-generator
 * @author shrimp
 * @table mdm_file_bucket (Bucket管理) DAO 接口，代码重新生成不覆盖
 */

@Mapper
public interface MdmFileBucketMapper extends BaseMapper<MdmFileBucket> {

    List<MdmFileBucket> getBucketList(MdmFileBucket entity);

    List<MdmFileBucket> getBucketOptions(MdmFileBucket entity);

}

