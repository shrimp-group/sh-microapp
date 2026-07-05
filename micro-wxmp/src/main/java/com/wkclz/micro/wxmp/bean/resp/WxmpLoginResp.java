package com.wkclz.micro.wxmp.bean.resp;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "微信公众号登录响应")
public class WxmpLoginResp {

    @Schema(description = "JWT令牌")
    private String token;

    @Schema(description = "用户编码")
    private String userCode;

    @Schema(description = "微信openId")
    private String openId;

    @Schema(description = "昵称")
    private String nickname;

    @Schema(description = "头像")
    private String avatar;
}
