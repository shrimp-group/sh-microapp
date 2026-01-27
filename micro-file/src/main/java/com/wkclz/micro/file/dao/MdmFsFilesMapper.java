package com.wkclz.micro.file.dao;

import com.wkclz.micro.file.pojo.entity.MdmFsFiles;
import com.wkclz.mybatis.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * Description Create by shrimp-gen
 * @author wangkaicun
 * @table mdm_fs_files (附件) DAO 接口，代码重新生成不覆盖
 */

@Mapper
public interface MdmFsFilesMapper extends BaseMapper<MdmFsFiles> {

    List<MdmFsFiles> getFileList4Page(MdmFsFiles entity);

    MdmFsFiles getFilesByFileId(@Param("fileId") String fileId);

    List<MdmFsFiles> getFilesByFileIds(@Param("fileIds") List<String> fileIds);

}

