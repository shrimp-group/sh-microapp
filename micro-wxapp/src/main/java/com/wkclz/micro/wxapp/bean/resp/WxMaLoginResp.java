package com.wkclz.micro.wxapp.bean.resp;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

@Data
@Schema(description = "微信小程序登录响应")
public class WxMaLoginResp implements Serializable {

    @Schema(description = "登录状态码")
    private Integer loginStatus;

    @Schema(description = "登录消息")
    private String loginMessage;

    @Schema(description = "JWT令牌")
    private String token;
}
