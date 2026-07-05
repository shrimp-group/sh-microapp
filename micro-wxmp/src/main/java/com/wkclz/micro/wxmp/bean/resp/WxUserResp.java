package com.wkclz.micro.wxmp.bean.resp;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "H5用户信息响应")
public class WxUserResp {

    @Schema(description = "用户编码")
    private String userCode;

    @Schema(description = "昵称")
    private String nickname;

    @Schema(description = "头像")
    private String avatar;

    @Schema(description = "性别(0未知1男2女)")
    private Integer gender;

    @Schema(description = "手机号")
    private String mobile;

    @Schema(description = "关注状态")
    private Integer subscribeStatus;
}
