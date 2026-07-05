package com.wkclz.micro.wxapp.bean.req;

import com.wkclz.web.bean.PageReq;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "微信小程序用户分页查询请求")
public class WxappUserPageReq extends PageReq {

    @Schema(description = "小程序appId")
    private String appId;

    @Schema(description = "客户昵称（模糊匹配）")
    private String nickname;

    @Schema(description = "手机号（模糊匹配）")
    private String mobile;

    @Schema(description = "微信openId（精确匹配）")
    private String openId;
}
