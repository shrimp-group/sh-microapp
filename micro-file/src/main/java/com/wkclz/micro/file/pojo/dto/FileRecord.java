package com.wkclz.micro.file.pojo.dto;

import com.wkclz.micro.file.pojo.entity.MdmFsFiles;
import lombok.Data;

@Data
public class FileRecord {


    private String orgFileName;

    private Integer index;
    private String  bucket;
    private String fileId;
    private MdmFsFiles file;
    private String previewUrl;
}
