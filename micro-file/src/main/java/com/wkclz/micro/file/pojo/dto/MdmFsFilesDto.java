package com.wkclz.micro.file.pojo.dto;

import com.wkclz.micro.file.pojo.entity.MdmFsFiles;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * Description Create by shrimp-gen
 * @author wangkaicun
 * @table mdm_fs_files (附件) 数据库实例扩展，代码重新生成不覆盖
 */

@Data
@EqualsAndHashCode(callSuper = false)
public class MdmFsFilesDto extends MdmFsFiles {


    private List<String> fileIds;

    private String previewUrl;

    private String fileName;
    private String fileType;


    /**
     * entity 转 Dto
     * @param source
     * @return
     */
    public static MdmFsFilesDto copy(MdmFsFiles source) {
        MdmFsFilesDto target = new MdmFsFilesDto();
        MdmFsFiles.copy(source, target);
        return target;
    }
}

