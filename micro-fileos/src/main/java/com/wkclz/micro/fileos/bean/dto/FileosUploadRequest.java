package com.wkclz.micro.fileos.bean.dto;

import lombok.Data;

@Data
public class FileosUploadRequest {

    private String category;
    private String bucketName;
    private Boolean isPublic;
    private String imageProcess;
    private String fileName;
}
