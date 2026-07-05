package com.wkclz.micro.wxapp.bean.resp;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

@Data
@Schema(description = "微信小程序用户信息响应")
public class WxMaUserInfoResp implements Serializable {

    @Schema(description = "用户编码")
    private String userCode;

    @Schema(description = "昵称")
    private String nickname;

    @Schema(description = "头像URL")
    private String avatar;

    @Schema(description = "微信openId")
    private String openId;

    @Schema(description = "手机号（脱敏）")
    private String mobile;
}
