package com.wkclz.micro.wxmp.pojo.vo;

import com.wkclz.core.annotation.Desc;
import lombok.Data;

@Data
public class WxMpAppInfo {

    @Desc("租户编码")
    private String tenantCode;

    @Desc("小程序appid")
    private String appId;

    @Desc("小程序Secret")
    private String appSecret;

    @Desc("证书文件cert")
    private String certPem;

    @Desc("证书文件key")
    private String keyPem;

    @Desc("小程序消息服务器配置token")
    private String mpToken;

    @Desc("小程序消息服务器配置EncodingAESKey")
    private String aesKey;

}
