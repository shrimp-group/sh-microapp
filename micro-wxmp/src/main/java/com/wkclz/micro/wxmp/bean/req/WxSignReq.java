package com.wkclz.micro.wxmp.bean.req;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "JSAPI签名请求")
public class WxSignReq {

    @NotBlank(message = "appId 不能为空")
    @Schema(description = "公众号appid")
    private String appId;

    @NotBlank(message = "url 不能为空")
    @Schema(description = "当前页面URL")
    private String url;
}
