package com.wkclz.micro.wxapp.bean.req;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

import java.io.Serializable;

@Data
@Schema(description = "微信小程序配置创建请求")
public class WxappConfigCreateReq implements Serializable {

    @NotBlank(message = "appId不能为空")
    @Pattern(regexp = "^wx[a-fA-F0-9]{16}$", message = "appId 格式错误!")
    @Schema(description = "小程序appId", requiredMode = Schema.RequiredMode.REQUIRED)
    private String appId;

    @NotBlank(message = "appSecret不能为空")
    @Schema(description = "小程序appSecret", requiredMode = Schema.RequiredMode.REQUIRED)
    private String appSecret;

    @Schema(description = "证书文件cert")
    private String certPem;

    @Schema(description = "证书文件key")
    private String keyPem;

    @Schema(description = "小程序消息服务器配置token")
    private String appToken;

    @Schema(description = "小程序消息服务器配置EncodingAESKey")
    private String aesKey;

    @Schema(description = "备注")
    private String remark;
}
