package com.wkclz.micro.fileos.bean.dto;

import lombok.Data;

@Data
public class PresignCompleteRequest {

    private String fileId;
    private String ossSp;
    private String bucketName;
    private String fileName;
    private Long fileSize;
    private String category;
    private Boolean isPublic;
    private String imageProcess;
}
