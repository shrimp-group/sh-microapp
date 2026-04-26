package com.wkclz.micro.file.bean.dto;

import com.wkclz.micro.file.bean.entity.MdmFileRecord;
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
