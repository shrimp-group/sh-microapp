package com.wkclz.micro.fileos.bean.dto;

import lombok.Data;

@Data
public class MultipartUploadInitRequest {

    private String fileName;
    private Long fileSize;
    private String contentType;
    private String category;
    private String bucketName;
    private Boolean isPublic;
    private Integer partCount;
    private Integer expireMinutes;
    private String imageProcess;
}
