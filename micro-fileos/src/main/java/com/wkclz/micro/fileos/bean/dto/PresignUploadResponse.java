package com.wkclz.micro.fileos.bean.dto;

import lombok.Data;

@Data
public class PresignUploadResponse {

    private String fileId;
    private String presignUrl;
    private String ossSp;
    private String bucketName;
    private String contentType;
    private Integer expireMinutes;
}
