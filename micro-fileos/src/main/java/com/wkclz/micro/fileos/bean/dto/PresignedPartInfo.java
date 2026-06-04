package com.wkclz.micro.fileos.bean.dto;

import lombok.Data;

@Data
public class PresignedPartInfo {

    private Integer partNumber;
    private String presignUrl;
}
