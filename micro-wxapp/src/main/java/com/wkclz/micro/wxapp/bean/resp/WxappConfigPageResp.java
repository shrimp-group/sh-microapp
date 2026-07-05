package com.wkclz.micro.wxapp.bean.resp;

import com.wkclz.web.bean.EntityResp;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "微信小程序配置分页列表响应")
public class WxappConfigPageResp extends EntityResp {

    @Schema(description = "租户编码")
    private String tenantCode;

    @Schema(description = "小程序appId")
    private String appId;

    @Schema(description = "小程序appSecret")
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
