package com.wkclz.micro.fileos.bean.dto;

import lombok.Data;

import java.util.List;

@Data
public class MultipartCompleteRequest {

    private String uploadId;
    private String fileId;
    private String bucketName;
    private String ossSp;
    private String fileName;
    private Long fileSize;
    private String category;
    private Boolean isPublic;
    private List<CompletedPartInfo> parts;
}
