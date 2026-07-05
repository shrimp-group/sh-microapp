package com.wkclz.micro.material.bean.resp;

import lombok.Data;

@Data
public class MaterialTransferLogResp {

    private Long id;
    private String materialCode;
    private String fromUserCode;
    private String toUserCode;
    private String operatorCode;
    private String createTime;
}
