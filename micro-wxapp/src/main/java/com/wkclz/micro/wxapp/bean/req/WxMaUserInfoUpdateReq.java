package com.wkclz.micro.wxapp.bean.req;

import com.wkclz.micro.wxapp.bean.req.validation.WxMaUserInfoUpdateValid;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

@Data
@Schema(description = "微信小程序用户信息更新请求")
@WxMaUserInfoUpdateValid
public class WxMaUserInfoUpdateReq implements Serializable {

    @Schema(description = "昵称")
    private String nickname;

    @Schema(description = "头像")
    private String avatar;
}
