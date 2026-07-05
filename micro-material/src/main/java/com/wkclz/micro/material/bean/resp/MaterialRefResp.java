package com.wkclz.micro.material.bean.resp;

import lombok.Data;

@Data
public class MaterialRefResp {

    private Long id;
    private String materialCode;
    private String bizType;
    private String bizCode;
    private String refDesc;
}
