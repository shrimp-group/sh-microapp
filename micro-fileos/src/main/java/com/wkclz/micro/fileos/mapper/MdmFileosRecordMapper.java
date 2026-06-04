package com.wkclz.micro.fileos.mapper;

import com.wkclz.micro.fileos.bean.entity.MdmFileosRecord;
import com.wkclz.mybatis.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface MdmFileosRecordMapper extends BaseMapper<MdmFileosRecord> {

    List<MdmFileosRecord> getRecordList4Page(MdmFileosRecord entity);

    MdmFileosRecord getRecordByFileId(@Param("fileId") String fileId, @Param("tenantCode") String tenantCode);

    List<MdmFileosRecord> getRecordByFileIds(@Param("fileIds") List<String> fileIds, @Param("tenantCode") String tenantCode);

    MdmFileosRecord getRecordByFileHash(@Param("fileHash") String fileHash, @Param("tenantCode") String tenantCode);

    List<MdmFileosRecord> getRecordByDirPath(@Param("dirPath") String dirPath, @Param("tenantCode") String tenantCode);

}
