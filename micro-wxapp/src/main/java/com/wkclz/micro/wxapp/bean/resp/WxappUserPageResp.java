package com.wkclz.micro.wxapp.bean.resp;

import com.wkclz.web.bean.EntityResp;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "微信小程序用户分页列表响应")
public class WxappUserPageResp extends EntityResp {

    @Schema(description = "用户编码")
    private String userCode;

    @Schema(description = "客户昵称")
    private String nickname;

    @Schema(description = "微信appId")
    private String appId;

    @Schema(description = "微信openId")
    private String openId;

    @Schema(description = "微信公众平台unionId")
    private String unionId;

    @Schema(description = "手机号")
    private String mobile;

    @Schema(description = "邮箱")
    private String email;

    @Schema(description = "性别(0未知1男2女)")
    private Integer gender;

    @Schema(description = "头像")
    private String avatar;

    @Schema(description = "登录成功次数")
    private Integer loginTimes;
}
