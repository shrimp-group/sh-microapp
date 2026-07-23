package com.wkclz.micro.wxapp.bean.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class WxMaAppInfo {

    @Schema(description = "租户编码")
    private String tenantCode;

    @Schema(description = "小程序appid")
    private String appId;

    @Schema(description = "小程序Secret")
    private String appSecret;

    @Schema(description = "证书文件cert")
    private String certPem;

    @Schema(description = "证书文件key")
    private String keyPem;

    @Schema(description = "小程序消息服务器配置token")
    private String appToken;

    @Schema(description = "小程序消息服务器配置EncodingAESKey")
    private String aesKey;


}
