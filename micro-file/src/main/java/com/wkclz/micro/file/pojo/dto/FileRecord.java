package com.wkclz.micro.file.pojo.dto;

import com.wkclz.micro.file.pojo.entity.MdmFileRecord;
import lombok.Data;

@Data
public class FileRecord {


    private String orgFileName;

    private Integer index;
    private String  bucket;
    private String fileId;
    private MdmFileRecord file;
    private String previewUrl;
}
