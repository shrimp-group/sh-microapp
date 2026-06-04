package com.wkclz.micro.fileos.bean.dto;

import lombok.Data;

import java.util.List;

@Data
public class MultipartUploadInitResponse {

    private String uploadId;
    private String fileId;
    private String ossSp;
    private String bucketName;
    private String contentType;
    private Integer expireMinutes;
    private List<PresignedPartInfo> parts;
}
