package com.wkclz.micro.material.bean.resp;

import lombok.Data;

@Data
public class MaterialVersionResp {

    private Long id;
    private String materialCode;
    private Integer versionNo;
    private String fileId;
    private String fileName;
    private Long fileSize;
    private String createTime;
    private String createBy;
}
