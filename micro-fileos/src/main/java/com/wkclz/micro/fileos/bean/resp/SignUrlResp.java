package com.wkclz.micro.fileos.bean.resp;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

@Data
@Schema(description = "签名URL响应")
public class SignUrlResp implements Serializable {

    @Schema(description = "签名URL")
    private String url;
}
