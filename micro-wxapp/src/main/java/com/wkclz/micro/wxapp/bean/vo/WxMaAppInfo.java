package com.wkclz.micro.wxapp.bean.vo;

import com.wkclz.core.annotation.FieldDesc;
import lombok.Data;

@Data
public class WxMaAppInfo {

    @FieldDesc("租户编码")
    private String tenantCode;

    @FieldDesc("小程序appid")
    private String appId;

    @FieldDesc("小程序Secret")
    private String appSecret;

    @FieldDesc("证书文件cert")
    private String certPem;

    @FieldDesc("证书文件key")
    private String keyPem;

    @FieldDesc("小程序消息服务器配置token")
    private String appToken;

    @FieldDesc("小程序消息服务器配置EncodingAESKey")
    private String aesKey;


}
