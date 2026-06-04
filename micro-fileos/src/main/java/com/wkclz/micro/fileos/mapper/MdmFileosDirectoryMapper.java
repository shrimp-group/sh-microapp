package com.wkclz.micro.fileos.mapper;

import com.wkclz.micro.fileos.bean.entity.MdmFileosDirectory;
import com.wkclz.mybatis.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface MdmFileosDirectoryMapper extends BaseMapper<MdmFileosDirectory> {

    List<MdmFileosDirectory> getDirectoryList(@Param("parentPath") String parentPath, @Param("bucketName") String bucketName, @Param("tenantCode") String tenantCode);

    List<MdmFileosDirectory> getDirectoryTree(@Param("bucketName") String bucketName, @Param("tenantCode") String tenantCode);

    MdmFileosDirectory getDirectoryByPath(@Param("dirPath") String dirPath, @Param("bucketName") String bucketName, @Param("tenantCode") String tenantCode);

}
