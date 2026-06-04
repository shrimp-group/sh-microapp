package com.wkclz.micro.fileos.mapper;

import com.wkclz.micro.fileos.bean.entity.MdmFileosMultipart;
import com.wkclz.mybatis.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import software.amazon.awssdk.services.s3.endpoints.internal.Value;

import java.util.Date;
import java.util.List;

@Mapper
public interface MdmFileosMultipartMapper extends BaseMapper<MdmFileosMultipart> {

    List<MdmFileosMultipart> getExpiredMultipartList(@Param("expireTime") Date expireTime);

    Integer updateMultipartFileStatus(MdmFileosMultipart entity);

}
