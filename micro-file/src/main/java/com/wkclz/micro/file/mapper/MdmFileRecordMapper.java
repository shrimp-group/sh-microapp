package com.wkclz.micro.file.mapper;

import com.wkclz.micro.file.pojo.entity.MdmFileRecord;
import com.wkclz.mybatis.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * Description Create by sh-generator
 * @author shrimp
 * @table mdm_file_record (附件) DAO 接口，代码重新生成不覆盖
 */

@Mapper
public interface MdmFileRecordMapper extends BaseMapper<MdmFileRecord> {

    List<MdmFileRecord> getFileList4Page(MdmFileRecord entity);

    MdmFileRecord getFilesByFileId(@Param("fileId") String fileId);

    List<MdmFileRecord> getFilesByFileIds(@Param("fileIds") List<String> fileIds);

}

